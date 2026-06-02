package de.u.project.voting;

/**
 * Raised when a voter who already submitted a ballot for a voting votes again.
 */
public class AlreadyVotedException extends RuntimeException {

    public AlreadyVotedException() {
        super("You have already voted in this voting.");
    }
}
