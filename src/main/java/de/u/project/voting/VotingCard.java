package de.u.project.voting;

import de.u.project.domain.Voting;

import io.quarkus.qute.TemplateData;

/**
 * A voting plus its aggregate counts, for list views.
 */
@TemplateData
public record VotingCard(Voting voting, long optionCount, long totalVotes) {
}
