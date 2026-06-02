package de.u.project.movie;

/**
 * Full short-stats view of a movie used when nominating it as an option.
 */
public record MovieDetail(
        String imdbId,
        String title,
        String year,
        String genre,
        String runtime,
        String imdbRating,
        String posterUrl) {
}
