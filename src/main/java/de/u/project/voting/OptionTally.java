package de.u.project.voting;

import de.u.project.domain.VotingOption;

import io.quarkus.qute.TemplateData;

/**
 * A voting option together with its current vote count.
 */
@TemplateData
public record OptionTally(VotingOption option, long votes) {
}
