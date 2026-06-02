package de.u.project.voting;

import de.u.project.domain.VotingOption;

/**
 * A voting option together with its current vote count.
 */
public record OptionTally(VotingOption option, long votes) {
}
