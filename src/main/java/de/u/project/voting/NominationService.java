package de.u.project.voting;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import de.u.project.domain.Voting;
import de.u.project.domain.VotingOption;
import de.u.project.domain.VotingOptionRepository;
import de.u.project.movie.MovieDetail;
import de.u.project.movie.MovieSearchService;

/**
 * Public, login-free nomination of a movie option into an OPEN voting.
 */
@ApplicationScoped
public class NominationService {

    @Inject
    VotingService votingService;

    @Inject
    VotingOptionRepository options;

    @Inject
    MovieSearchService movieSearch;

    public VotingOption nominate(UUID votingId, String imdbId) {
        Voting voting = votingService.get(votingId);
        if (!voting.isOpen()) {
            throw new VotingClosedException();
        }
        if (imdbId == null || imdbId.isBlank()) {
            throw new IllegalArgumentException("Missing movie selection.");
        }
        if (options.existsByVotingAndImdbId(voting, imdbId)) {
            throw new DuplicateOptionException(imdbId);
        }

        // OMDb call is outside the transaction so it does not hold a DB connection.
        MovieDetail detail = movieSearch.getDetail(imdbId);

        return persistOption(votingId, detail);
    }

    @Transactional
    VotingOption persistOption(UUID votingId, MovieDetail detail) {
        Voting voting = votingService.get(votingId);
        if (!voting.isOpen()) {
            throw new VotingClosedException();
        }
        if (options.existsByVotingAndImdbId(voting, detail.imdbId())) {
            throw new DuplicateOptionException(detail.imdbId());
        }
        VotingOption option = new VotingOption();
        option.voting = voting;
        option.imdbId = detail.imdbId();
        option.title = detail.title();
        option.year = detail.year();
        option.genre = detail.genre();
        option.runtime = detail.runtime();
        option.imdbRating = detail.imdbRating();
        option.posterUrl = detail.posterUrl();
        options.persist(option);
        return option;
    }
}
