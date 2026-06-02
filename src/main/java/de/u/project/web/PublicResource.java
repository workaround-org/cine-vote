package de.u.project.web;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

import de.u.project.domain.BallotRepository;
import de.u.project.domain.Voting;
import de.u.project.movie.MovieResult;
import de.u.project.movie.MovieSearchException;
import de.u.project.movie.MovieSearchService;
import de.u.project.voting.AlreadyVotedException;
import de.u.project.voting.DuplicateOptionException;
import de.u.project.voting.NominationService;
import de.u.project.voting.VoteService;
import de.u.project.voting.VotingClosedException;
import de.u.project.voting.VotingQueryService;
import de.u.project.voting.VotingService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class PublicResource {

    private static final String VOTER_COOKIE = "voter_id";

    @Inject
    Template index;

    @Inject
    Template voting;

    @Inject
    Template search;

    @Inject
    VotingService votingService;

    @Inject
    VotingQueryService queryService;

    @Inject
    NominationService nominationService;

    @Inject
    VoteService voteService;

    @Inject
    MovieSearchService movieSearch;

    @Inject
    BallotRepository ballots;

    @GET
    public TemplateInstance listVotings() {
        return index.data("cards", queryService.cards());
    }

    @GET
    @Path("/votings/{id}")
    public TemplateInstance viewVoting(@PathParam("id") UUID id,
            @CookieParam(VOTER_COOKIE) String voterId,
            @RestQuery String error) {
        Voting v = votingService.get(id);
        boolean alreadyVoted = voterId != null && ballots.hasVoted(v, voterId);
        return voting.data("voting", v)
                .data("tallies", votingService.results(id))
                .data("alreadyVoted", alreadyVoted)
                .data("error", error);
    }

    @GET
    @Path("/search")
    public TemplateInstance searchMovies(@RestQuery UUID votingId, @RestQuery String q) {
        List<MovieResult> results = List.of();
        String error = null;
        if (q != null && !q.isBlank()) {
            try {
                results = movieSearch.search(q);
            } catch (MovieSearchException e) {
                error = e.getMessage();
            }
        }
        return search.data("votingId", votingId)
                .data("q", q)
                .data("results", results)
                .data("error", error);
    }

    @POST
    @Path("/votings/{id}/options")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response nominate(@PathParam("id") UUID id, @RestForm String imdbId) {
        try {
            nominationService.nominate(id, imdbId);
            return redirect("/votings/" + id, null);
        } catch (DuplicateOptionException e) {
            return redirect("/votings/" + id, "That movie is already an option in this voting.");
        } catch (VotingClosedException e) {
            return redirect("/votings/" + id, "This voting is closed.");
        } catch (MovieSearchException e) {
            return redirect("/search?votingId=" + id, e.getMessage());
        }
    }

    @POST
    @Path("/votings/{id}/votes")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response vote(@PathParam("id") UUID id,
            @RestForm("optionId") List<UUID> optionIds,
            @CookieParam(VOTER_COOKIE) String voterId) {
        String voter = voterId != null ? voterId : UUID.randomUUID().toString();
        NewCookie cookie = new NewCookie.Builder(VOTER_COOKIE)
                .value(voter)
                .path("/")
                .httpOnly(true)
                .maxAge(60 * 60 * 24 * 365)
                .build();
        try {
            voteService.castBallot(id, optionIds, voter);
            return redirect("/votings/" + id, null, cookie);
        } catch (IllegalArgumentException e) {
            return redirect("/votings/" + id, "Select at least one option.", cookie);
        } catch (VotingClosedException e) {
            return redirect("/votings/" + id, "This voting is closed.", cookie);
        } catch (AlreadyVotedException e) {
            return redirect("/votings/" + id, "You have already voted in this voting.", cookie);
        }
    }

    private Response redirect(String path, String error) {
        return redirect(path, error, null);
    }

    private Response redirect(String path, String error, NewCookie cookie) {
        String location = path;
        if (error != null) {
            String sep = path.contains("?") ? "&" : "?";
            location = path + sep + "error=" + URLEncoder.encode(error, StandardCharsets.UTF_8);
        }
        Response.ResponseBuilder builder = Response.seeOther(URI.create(location));
        if (cookie != null) {
            builder.cookie(cookie);
        }
        return builder.build();
    }
}
