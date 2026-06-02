package de.u.project.movie;

/**
 * A single movie from a search result list.
 */
public record MovieResult(String imdbId, String title, String year, String posterUrl) {
}
