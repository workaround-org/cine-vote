package de.u.project.voting;

import de.u.project.domain.Voting;

/**
 * A voting plus its aggregate counts, for list views.
 */
public record VotingCard(Voting voting, long optionCount, long totalVotes) {
}
