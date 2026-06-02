package de.u.project.movie;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.rest.client.reactive.ClientQueryParam;

/**
 * OMDb HTTP client. The API key is appended to every request from configuration.
 */
@RegisterRestClient(configKey = "omdb")
@ClientQueryParam(name = "apikey", value = "${cinevote.omdb.api-key}")
@Path("/")
public interface OmdbClient {

    @GET
    OmdbSearchResponse search(@QueryParam("s") String title);

    @GET
    OmdbMovieResponse findById(@QueryParam("i") String imdbId);
}
