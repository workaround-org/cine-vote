package de.u.project.voting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
import de.u.project.movie.MovieDetail;
import de.u.project.movie.MovieSearchService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class NominationServiceTest {

    @Inject
    NominationService nomination;

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

    @InjectMock
    MovieSearchService movieSearch;

    @BeforeEach
    @Transactional
    void clean() {
        votes.deleteAll();
        ballots.deleteAll();
        options.deleteAll();
        votings.deleteAll();
        when(movieSearch.getDetail(anyString())).thenReturn(
                new MovieDetail("tt1375666", "Inception", "2010", "Sci-Fi", "148 min", "8.8", "http://poster"));
    }

    @Test
    void nominateStoresMovieSnapshot() {
        Voting v = votingService.create("Friday Night", null);

        VotingOption option = nomination.nominate(v.id, "tt1375666");

        assertThat(option.id).isNotNull();
        assertThat(option.title).isEqualTo("Inception");
        assertThat(option.genre).isEqualTo("Sci-Fi");
        assertThat(option.imdbRating).isEqualTo("8.8");
        assertThat(option.posterUrl).isEqualTo("http://poster");
    }

    @Test
    void nominateOnClosedVotingIsRejected() {
        Voting v = votingService.create("Friday Night", null);
        votingService.end(v.id);

        assertThatThrownBy(() -> nomination.nominate(v.id, "tt1375666"))
                .isInstanceOf(VotingClosedException.class);
    }

    @Test
    void duplicateImdbIdIsRejected() {
        Voting v = votingService.create("Friday Night", null);
        nomination.nominate(v.id, "tt1375666");

        assertThatThrownBy(() -> nomination.nominate(v.id, "tt1375666"))
                .isInstanceOf(DuplicateOptionException.class);
    }
}
