package de.u.project.voting;

/**
 * Raised when an action (nominate, vote) targets a voting that is not OPEN.
 */
public class VotingClosedException extends RuntimeException {

    public VotingClosedException() {
        super("This voting is closed.");
    }
}
