package de.u.project.movie;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Raw OMDb response for a title search ({@code ?s=}).
 */
public class OmdbSearchResponse {

    @JsonProperty("Search")
    public List<Item> search;

    @JsonProperty("Response")
    public String response;

    @JsonProperty("Error")
    public String error;

    public boolean isSuccess() {
        return "True".equalsIgnoreCase(response);
    }

    public static class Item {
        @JsonProperty("Title")
        public String title;

        @JsonProperty("Year")
        public String year;

        @JsonProperty("imdbID")
        public String imdbId;

        @JsonProperty("Poster")
        public String poster;
    }
}
