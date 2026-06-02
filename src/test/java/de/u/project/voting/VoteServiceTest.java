package de.u.project.voting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.u.project.domain.BallotRepository;
import de.u.project.domain.VoteRepository;
import de.u.project.domain.Voting;
import de.u.project.domain.VotingOption;
import de.u.project.domain.VotingOptionRepository;
import de.u.project.domain.VotingRepository;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class VoteServiceTest {

    @Inject
    VoteService voteService;

    @Inject
    VotingService votingService;

    @Inject
    VotingRepository votings;

    @Inject
    VotingOptionRepository options;

    @Inject
    VoteRepository votes;

    @Inject
    BallotRepository ballots;

    private Voting voting;
    private VotingOption optionA;
    private VotingOption optionB;

    @BeforeEach
    @Transactional
    void setup() {
        votes.deleteAll();
        ballots.deleteAll();
        options.deleteAll();
        votings.deleteAll();

        voting = new Voting();
        voting.title = "Friday Night";
        votings.persist(voting);
        optionA = newOption("tt1", "A");
        optionB = newOption("tt2", "B");
    }

    private VotingOption newOption(String imdbId, String title) {
        VotingOption o = new VotingOption();
        o.voting = voting;
        o.imdbId = imdbId;
        o.title = title;
        options.persist(o);
        return o;
    }

    @Test
    void multiOptionBallotIncrementsEachSelectedOption() {
        voteService.castBallot(voting.id, List.of(optionA.id, optionB.id), "voter-1");

        assertThat(votes.countByOption(optionA)).isEqualTo(1);
        assertThat(votes.countByOption(optionB)).isEqualTo(1);
    }

    @Test
    void emptySelectionIsRejected() {
        assertThatThrownBy(() -> voteService.castBallot(voting.id, Set.of(), "voter-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void closedVotingRejectsBallot() {
        votingService.end(voting.id);

        assertThatThrownBy(() -> voteService.castBallot(voting.id, List.of(optionA.id), "voter-1"))
                .isInstanceOf(VotingClosedException.class);
    }

    @Test
    void firstBallotAcceptedRepeatRejected() {
        voteService.castBallot(voting.id, List.of(optionA.id), "voter-1");

        assertThatThrownBy(() -> voteService.castBallot(voting.id, List.of(optionB.id), "voter-1"))
                .isInstanceOf(AlreadyVotedException.class);
        // original vote unchanged, no vote added for optionB
        assertThat(votes.countByOption(optionA)).isEqualTo(1);
        assertThat(votes.countByOption(optionB)).isEqualTo(0);
    }

    @Test
    void differentVotersEachGetABallot() {
        voteService.castBallot(voting.id, List.of(optionA.id), "voter-1");
        voteService.castBallot(voting.id, List.of(optionA.id), "voter-2");

        assertThat(votes.countByOption(optionA)).isEqualTo(2);
    }
}
