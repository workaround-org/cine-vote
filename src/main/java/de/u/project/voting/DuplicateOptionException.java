package de.u.project.voting;

/**
 * Raised when a movie (by IMDb id) is already an option in the target voting.
 */
public class DuplicateOptionException extends RuntimeException {

    public DuplicateOptionException(String imdbId) {
        super("This movie is already an option in this voting: " + imdbId);
    }
}
