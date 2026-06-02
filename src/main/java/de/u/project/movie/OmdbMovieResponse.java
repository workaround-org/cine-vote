package de.u.project.movie;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Raw OMDb response for a by-id lookup ({@code ?i=}).
 */
public class OmdbMovieResponse {

    @JsonProperty("Title")
    public String title;

    @JsonProperty("Year")
    public String year;

    @JsonProperty("Genre")
    public String genre;

    @JsonProperty("Runtime")
    public String runtime;

    @JsonProperty("imdbRating")
    public String imdbRating;

    @JsonProperty("imdbID")
    public String imdbId;

    @JsonProperty("Poster")
    public String poster;

    @JsonProperty("Response")
    public String response;

    @JsonProperty("Error")
    public String error;

    public boolean isSuccess() {
        return "True".equalsIgnoreCase(response);
    }
}
