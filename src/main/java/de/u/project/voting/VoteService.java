package de.u.project.voting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;

import de.u.project.domain.Ballot;
import de.u.project.domain.BallotRepository;
import de.u.project.domain.Vote;
import de.u.project.domain.Voting;
import de.u.project.domain.VotingOption;
import de.u.project.domain.VotingOptionRepository;
import de.u.project.domain.VoteRepository;

/**
 * Public, login-free approval voting: one ballot per voter per voting, each
 * selected option receives one vote.
 */
@ApplicationScoped
public class VoteService {

    @Inject
    VotingService votingService;

    @Inject
    VotingOptionRepository options;

    @Inject
    VoteRepository votes;

    @Inject
    BallotRepository ballots;

    /**
     * Casts an approval ballot. Each selected option gets one vote.
     *
     * @throws VotingClosedException    if the voting is not OPEN
     * @throws IllegalArgumentException if no option is selected
     * @throws AlreadyVotedException    if this voter already voted in this voting
     */
    @Transactional
    public void castBallot(UUID votingId, Collection<UUID> optionIds, String voterId) {
        Voting voting = votingService.get(votingId);
        if (!voting.isOpen()) {
            throw new VotingClosedException();
        }
        if (optionIds == null || optionIds.isEmpty()) {
            throw new IllegalArgumentException("Select at least one option.");
        }
        if (ballots.hasVoted(voting, voterId)) {
            throw new AlreadyVotedException();
        }

        // Validate every option belongs to this voting before any write.
        List<VotingOption> selected = new ArrayList<>();
        for (UUID optionId : optionIds) {
            VotingOption option = options.findById(optionId);
            if (option == null || !option.voting.id.equals(voting.id)) {
                throw new IllegalArgumentException("Invalid option selected: " + optionId);
            }
            selected.add(option);
        }

        Ballot ballot = new Ballot();
        ballot.voting = voting;
        ballot.voterId = voterId;
        try {
            ballots.persistAndFlush(ballot);
        } catch (PersistenceException e) {
            // Lost the race against a concurrent ballot from the same voter.
            throw new AlreadyVotedException();
        }

        for (VotingOption option : selected) {
            Vote vote = new Vote();
            vote.option = option;
            vote.voterId = voterId;
            votes.persist(vote);
        }
    }
}
