package de.u.project.web;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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
import de.u.project.voting.VotingService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class PublicFlowTest {

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

    private Voting openVoting() {
        return votingService.create("Friday Night", "Pick a movie");
    }

    @Test
    void votingPageRendersTitle() {
        Voting v = openVoting();
        given().when().get("/votings/" + v.id)
                .then().statusCode(200)
                .body(containsString("Friday Night"));
    }

    @Test
    void nominateAddsOptionWithSnapshot() {
        Voting v = openVoting();

        given().contentType("application/x-www-form-urlencoded")
                .formParam("imdbId", "tt1375666")
                .redirects().follow(false)
                .when().post("/votings/" + v.id + "/options")
                .then().statusCode(303);

        assertThat(options.count()).isEqualTo(1);
        VotingOption opt = options.listAll().get(0);
        assertThat(opt.title).isEqualTo("Inception");
        assertThat(opt.imdbRating).isEqualTo("8.8");
    }

    @Test
    void voteIncrementsTallyAndBlocksRepeat() {
        Voting v = openVoting();
        VotingOption opt = addOption(v, "tt1375666", "Inception");

        String voterCookie = given().contentType("application/x-www-form-urlencoded")
                .formParam("optionId", opt.id.toString())
                .redirects().follow(false)
                .when().post("/votings/" + v.id + "/votes")
                .then().statusCode(303)
                .extract().cookie("voter_id");

        assertThat(votes.countByOption(opt)).isEqualTo(1);

        // repeat with same voter cookie -> rejected, count unchanged
        given().contentType("application/x-www-form-urlencoded")
                .cookie("voter_id", voterCookie)
                .formParam("optionId", opt.id.toString())
                .redirects().follow(false)
                .when().post("/votings/" + v.id + "/votes")
                .then().statusCode(303)
                .header("Location", containsString("error"));

        assertThat(votes.countByOption(opt)).isEqualTo(1);
    }

    @Transactional
    VotingOption addOption(Voting voting, String imdbId, String title) {
        Voting attached = votings.findById(voting.id);
        VotingOption o = new VotingOption();
        o.voting = attached;
        o.imdbId = imdbId;
        o.title = title;
        options.persist(o);
        return o;
    }
}
