package de.u.project.movie;

import io.quarkus.qute.TemplateData;

/**
 * A single movie from a search result list.
 */
@TemplateData
public record MovieResult(String imdbId, String title, String year, String posterUrl) {
}
