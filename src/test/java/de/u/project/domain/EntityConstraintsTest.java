package de.u.project.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;

import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class EntityConstraintsTest {

    @Inject
    VotingRepository votings;

    @Inject
    VotingOptionRepository options;

    @Inject
    BallotRepository ballots;

    @Inject
    VoteRepository votes;

    private Voting newVoting(String title) {
        Voting v = new Voting();
        v.title = title;
        votings.persist(v);
        return v;
    }

    private VotingOption newOption(Voting voting, String imdbId, String title) {
        VotingOption o = new VotingOption();
        o.voting = voting;
        o.imdbId = imdbId;
        o.title = title;
        options.persist(o);
        return o;
    }

    @Test
    @TestTransaction
    void votingGetsGeneratedUuid() {
        Voting v = newVoting("Friday Night");
        votings.flush();

        assertThat(v.id).isNotNull();
        assertThat(v.state).isEqualTo(VotingState.OPEN);
        assertThat(v.createdAt).isNotNull();
    }

    @Test
    @TestTransaction
    void duplicateMovieInSameVotingIsRejected() {
        Voting v = newVoting("Friday Night");
        newOption(v, "tt0111161", "The Shawshank Redemption");

        assertThatThrownBy(() -> {
            newOption(v, "tt0111161", "The Shawshank Redemption");
            options.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @TestTransaction
    void sameMovieInDifferentVotingsIsAllowed() {
        Voting a = newVoting("Voting A");
        Voting b = newVoting("Voting B");
        newOption(a, "tt0111161", "The Shawshank Redemption");
        newOption(b, "tt0111161", "The Shawshank Redemption");
        options.flush();

        assertThat(options.count()).isEqualTo(2);
    }

    @Test
    @TestTransaction
    void duplicateBallotForSameVoterAndVotingIsRejected() {
        Voting v = newVoting("Friday Night");

        Ballot first = new Ballot();
        first.voting = v;
        first.voterId = "voter-1";
        ballots.persist(first);

        assertThatThrownBy(() -> {
            Ballot dup = new Ballot();
            dup.voting = v;
            dup.voterId = "voter-1";
            ballots.persist(dup);
            ballots.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @TestTransaction
    void voteCountPerOptionIsTracked() {
        Voting v = newVoting("Friday Night");
        VotingOption o = newOption(v, "tt0111161", "The Shawshank Redemption");

        Vote vote = new Vote();
        vote.option = o;
        vote.voterId = "voter-1";
        votes.persist(vote);
        votes.flush();

        assertThat(votes.countByOption(o)).isEqualTo(1);
    }
}
