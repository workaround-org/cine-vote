package de.u.project.movie;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import io.quarkus.cache.CacheResult;

/**
 * Searches OMDb and maps raw responses to internal DTOs. Network/key failures
 * are wrapped in {@link MovieSearchException} so callers can show a retry UI
 * without persisting anything.
 *
 * <p>Successful lookups are cached (Caffeine) to cut external OMDb calls for
 * repeated terms/ids. Blank-input shortcuts and failures are never cached:
 * {@code @CacheResult} stores returned values only, not thrown exceptions.
 */
@ApplicationScoped
public class MovieSearchService {

    private static final Logger LOG = Logger.getLogger(MovieSearchService.class);
    private static final String NOT_AVAILABLE = "N/A";
    private static final Pattern OMDB_ERROR = Pattern.compile("\"Error\"\\s*:\\s*\"([^\"]*)\"");

    @Inject
    @RestClient
    OmdbClient omdbClient;

    @ConfigProperty(name = "cinevote.omdb.api-key")
    String configuredApiKey;

    /**
     * Self-reference (the Arc client proxy) used to invoke the {@code @CacheResult}
     * delegates. Calling them via {@code this} would target the raw bean instance and
     * the cache interceptor would not be guaranteed to fire; the injected proxy does.
     */
    @Inject
    MovieSearchService self;

    /**
     * Searches movies by title. Returns an empty list for a blank term (no OMDb
     * call) or when OMDb reports no matches.
     */
    public List<MovieResult> search(String term) {
        if (term == null || term.isBlank()) {
            return List.of();
        }
        return self.searchCached(term.trim());
    }

    /**
     * Cached delegate keyed on the normalized term. Only reached for non-blank
     * input, so empty-input shortcuts stay out of the cache.
     */
    @CacheResult(cacheName = "omdb-search")
    List<MovieResult> searchCached(String term) {
        OmdbSearchResponse response;
        try {
            response = omdbClient.search(apiKey(), term);
        } catch (WebApplicationException wae) {
            String detail = describeOmdb(wae);
            LOG.warnf(wae, "OMDb search failed for term '%s' — %s", term, detail);
            throw new MovieSearchException("Movie search failed (" + detail + ").", wae);
        } catch (RuntimeException e) {
            LOG.warnf(e, "OMDb search failed for term '%s'", term);
            throw new MovieSearchException("Movie search is currently unavailable. Please try again.", e);
        }
        if (response == null || !response.isSuccess() || response.search == null) {
            return List.of();
        }
        return response.search.stream()
                .map(i -> new MovieResult(i.imdbId, i.title, i.year, clean(i.poster)))
                .toList();
    }

    /**
     * Fetches full short stats for a movie by its IMDb id.
     */
    public MovieDetail getDetail(String imdbId) {
        if (imdbId == null || imdbId.isBlank()) {
            throw new MovieSearchException("Missing IMDb id.", null);
        }
        return self.detailCached(imdbId.trim());
    }

    /**
     * Cached delegate keyed on the IMDb id. Failures throw and are not cached.
     */
    @CacheResult(cacheName = "omdb-detail")
    MovieDetail detailCached(String imdbId) {
        OmdbMovieResponse response;
        try {
            response = omdbClient.findById(apiKey(), imdbId);
        } catch (WebApplicationException wae) {
            String detail = describeOmdb(wae);
            LOG.warnf(wae, "OMDb detail lookup failed for id '%s' — %s", imdbId, detail);
            throw new MovieSearchException("Movie lookup failed (" + detail + ").", wae);
        } catch (RuntimeException e) {
            LOG.warnf(e, "OMDb detail lookup failed for id '%s'", imdbId);
            throw new MovieSearchException("Movie lookup is currently unavailable. Please try again.", e);
        }
        if (response == null || !response.isSuccess()) {
            throw new MovieSearchException("Movie not found for id " + imdbId, null);
        }
        return new MovieDetail(
                response.imdbId,
                response.title,
                clean(response.year),
                clean(response.genre),
                clean(response.runtime),
                clean(response.imdbRating),
                clean(response.poster));
    }

    private String apiKey() {
        return sanitizeKey(configuredApiKey);
    }

    /**
     * Normalizes the configured OMDb key: strips surrounding whitespace and line
     * endings (a trailing CR from a CRLF {@code .env} file is a common culprit) and
     * any wrapping quotes. A stray byte here corrupts the query URL and yields an
     * upstream Cloudflare 400 instead of OMDb's own JSON error.
     */
    static String sanitizeKey(String raw) {
        if (raw == null) {
            return "";
        }
        String key = raw.strip();
        if (key.length() >= 2
                && ((key.startsWith("\"") && key.endsWith("\"")) || (key.startsWith("'") && key.endsWith("'")))) {
            key = key.substring(1, key.length() - 1).strip();
        }
        return key;
    }

    private static String clean(String value) {
        if (value == null || value.isBlank() || NOT_AVAILABLE.equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }

    /**
     * Builds a diagnostic string from a failed OMDb HTTP response: status code plus
     * OMDb's own {@code Error} message (e.g. "Invalid API key!"). The bare rest-client
     * exception only carries "Bad Request, status code 400" — the useful reason lives
     * in the response body, which this reads.
     */
    private static String describeOmdb(WebApplicationException wae) {
        Response r = wae.getResponse();
        if (r == null) {
            return wae.getMessage();
        }
        int status = r.getStatus();
        String body = readBody(r);
        String error = extractError(body);
        if (error != null && !error.isBlank()) {
            return "HTTP " + status + ": " + error;
        }
        return body.isBlank() ? "HTTP " + status : "HTTP " + status + ": " + body.strip();
    }

    private static String readBody(Response r) {
        try {
            if (r.hasEntity()) {
                return r.readEntity(String.class);
            }
        } catch (RuntimeException ignore) {
            // Buffered/built responses may reject readEntity; fall back to the raw entity.
            Object entity = r.getEntity();
            if (entity != null) {
                return entity.toString();
            }
        }
        return "";
    }

    private static String extractError(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        Matcher m = OMDB_ERROR.matcher(body);
        return m.find() ? m.group(1) : null;
    }
}
