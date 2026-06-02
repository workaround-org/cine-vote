package de.u.project.voting;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import de.u.project.domain.Voting;

/**
 * Read helpers that assemble view models (cards) for list pages.
 */
@ApplicationScoped
public class VotingQueryService {

    @Inject
    VotingService votingService;

    @Transactional
    public List<VotingCard> cards() {
        return votingService.listAll().stream()
                .map(v -> new VotingCard(v, votingService.optionCount(v), votingService.totalVotes(v)))
                .toList();
    }
}
