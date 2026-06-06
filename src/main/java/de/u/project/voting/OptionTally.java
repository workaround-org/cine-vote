package de.u.project.voting;

import de.u.project.domain.VotingOption;

import io.quarkus.qute.TemplateData;

/**
 * A voting option together with its current vote count, its share of all
 * votes (0-100, for result bars) and whether it is (one of) the current
 * leader(s).
 */
@TemplateData
public record OptionTally(VotingOption option, long votes, int percent, boolean leading) {
}
