package de.u.project.movie;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

/**
 * Searches OMDb and maps raw responses to internal DTOs. Network/key failures
 * are wrapped in {@link MovieSearchException} so callers can show a retry UI
 * without persisting anything.
 */
@ApplicationScoped
public class MovieSearchService {

    private static final Logger LOG = Logger.getLogger(MovieSearchService.class);
    private static final String NOT_AVAILABLE = "N/A";

    @Inject
    @RestClient
    OmdbClient omdbClient;

    /**
     * Searches movies by title. Returns an empty list for a blank term (no OMDb
     * call) or when OMDb reports no matches.
     */
    public List<MovieResult> search(String term) {
        if (term == null || term.isBlank()) {
            return List.of();
        }
        OmdbSearchResponse response;
        try {
            response = omdbClient.search(term.trim());
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
        OmdbMovieResponse response;
        try {
            response = omdbClient.findById(imdbId.trim());
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

    private static String clean(String value) {
        if (value == null || value.isBlank() || NOT_AVAILABLE.equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }
}
