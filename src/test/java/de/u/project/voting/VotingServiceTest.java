package de.u.project.voting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.u.project.domain.BallotRepository;
import de.u.project.domain.Vote;
import de.u.project.domain.VoteRepository;
import de.u.project.domain.Voting;
import de.u.project.domain.VotingOption;
import de.u.project.domain.VotingOptionRepository;
import de.u.project.domain.VotingRepository;
import de.u.project.domain.VotingState;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class VotingServiceTest {

    @Inject
    VotingService service;

    @Inject
    VotingRepository votings;

    @Inject
    VotingOptionRepository options;

    @Inject
    VoteRepository votes;

    @Inject
    BallotRepository ballots;

    @BeforeEach
    @Transactional
    void clean() {
        votes.deleteAll();
        ballots.deleteAll();
        options.deleteAll();
        votings.deleteAll();
    }

    @Test
    void createPersistsOpenVoting() {
        Voting v = service.create("Friday Night", "Pick a movie");

        assertThat(v.id).isNotNull();
        assertThat(v.state).isEqualTo(VotingState.OPEN);
        assertThat(v.description).isEqualTo("Pick a movie");
    }

    @Test
    void createRejectsBlankTitle() {
        assertThatThrownBy(() -> service.create("  ", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listReturnsAllVotings() {
        service.create("A", null);
        service.create("B", null);

        assertThat(service.listAll()).hasSize(2);
    }

    @Test
    void endClosesOpenVoting() {
        Voting v = service.create("Friday Night", null);

        Voting closed = service.end(v.id);

        assertThat(closed.state).isEqualTo(VotingState.CLOSED);
        assertThat(closed.closedAt).isNotNull();
    }

    @Test
    void endingClosedVotingIsRejected() {
        Voting v = service.create("Friday Night", null);
        service.end(v.id);

        assertThatThrownBy(() -> service.end(v.id))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resultsAreRankedByVoteCountDescending() {
        Voting v = service.create("Friday Night", null);
        VotingOption low = newOption(v, "tt1", "Low");
        VotingOption high = newOption(v, "tt2", "High");
        addVotes(high, 3);
        addVotes(low, 1);

        List<OptionTally> results = service.results(v.id);

        assertThat(results).extracting(t -> t.option().id).containsExactly(high.id, low.id);
        assertThat(results.get(0).votes()).isEqualTo(3);
        assertThat(results.get(1).votes()).isEqualTo(1);
        assertThat(service.totalVotes(v)).isEqualTo(4);
        assertThat(service.optionCount(v)).isEqualTo(2);
    }

    @Test
    void resultsIncludeSharePercentAndLeader() {
        Voting v = service.create("Friday Night", null);
        VotingOption low = newOption(v, "tt1", "Low");
        VotingOption high = newOption(v, "tt2", "High");
        addVotes(high, 3);
        addVotes(low, 1);

        List<OptionTally> results = service.results(v.id);

        assertThat(results.get(0).percent()).isEqualTo(75);
        assertThat(results.get(0).leading()).isTrue();
        assertThat(results.get(1).percent()).isEqualTo(25);
        assertThat(results.get(1).leading()).isFalse();
    }

    @Test
    void resultsWithoutVotesHaveZeroPercentAndNoLeader() {
        Voting v = service.create("Friday Night", null);
        newOption(v, "tt1", "Only");

        List<OptionTally> results = service.results(v.id);

        assertThat(results.get(0).percent()).isZero();
        assertThat(results.get(0).leading()).isFalse();
    }

    @Transactional
    VotingOption newOption(Voting voting, String imdbId, String title) {
        Voting attached = votings.findById(voting.id);
        VotingOption o = new VotingOption();
        o.voting = attached;
        o.imdbId = imdbId;
        o.title = title;
        options.persist(o);
        return o;
    }

    @Transactional
    void addVotes(VotingOption option, int count) {
        VotingOption attached = options.findById(option.id);
        for (int i = 0; i < count; i++) {
            Vote vote = new Vote();
            vote.option = attached;
            vote.voterId = UUID.randomUUID().toString();
            votes.persist(vote);
        }
    }
}
