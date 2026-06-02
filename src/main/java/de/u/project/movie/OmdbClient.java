package de.u.project.movie;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * OMDb HTTP client. The API key is passed explicitly per call (sanitized by
 * {@link MovieSearchService}) rather than injected from raw config, so stray
 * whitespace/quotes in the key cannot corrupt the request URL.
 *
 * <p>A {@code User-Agent} is set explicitly: the Quarkus reactive client sends
 * none by default, and OMDb's Cloudflare front rejects User-Agent-less requests
 * with an HTTP 400 before they reach the API.
 */
@RegisterRestClient(configKey = "omdb")
@ClientHeaderParam(name = "User-Agent", value = "cine-vote/1.0 (+https://github.com/workaround-org/cine-vote)")
@Path("/")
public interface OmdbClient {

    @GET
    OmdbSearchResponse search(@QueryParam("apikey") String apiKey, @QueryParam("s") String title);

    @GET
    OmdbMovieResponse findById(@QueryParam("apikey") String apiKey, @QueryParam("i") String imdbId);
}
