package de.u.project.movie;

/**
 * Thrown when OMDb cannot be reached or returns an unusable response. Callers
 * should surface a friendly retry message and persist nothing.
 */
public class MovieSearchException extends RuntimeException {

    public MovieSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
