package de.u.project.voting;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import de.u.project.domain.Voting;
import de.u.project.domain.VotingOption;
import de.u.project.domain.VotingOptionRepository;
import de.u.project.domain.VotingRepository;
import de.u.project.domain.VotingState;
import de.u.project.domain.VoteRepository;

/**
 * Admin-side lifecycle of a voting: create, list, end, and tally results.
 */
@ApplicationScoped
public class VotingService {

    @Inject
    VotingRepository votings;

    @Inject
    VotingOptionRepository options;

    @Inject
    VoteRepository votes;

    @Transactional
    public Voting create(String title, String description) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Voting title must not be blank.");
        }
        Voting v = new Voting();
        v.title = title.trim();
        v.description = description == null || description.isBlank() ? null : description.trim();
        votings.persist(v);
        return v;
    }

    public List<Voting> listAll() {
        return votings.listAll(io.quarkus.panache.common.Sort.by("createdAt").descending());
    }

    public Voting get(UUID id) {
        Voting v = votings.findById(id);
        if (v == null) {
            throw new NotFoundException("Voting not found: " + id);
        }
        return v;
    }

    @Transactional
    public Voting end(UUID id) {
        Voting v = get(id);
        if (v.state != VotingState.OPEN) {
            throw new IllegalStateException("Voting is already closed.");
        }
        v.state = VotingState.CLOSED;
        v.closedAt = Instant.now();
        return v;
    }

    /**
     * Options of the voting ranked by vote count, descending.
     */
    public List<OptionTally> results(UUID votingId) {
        Voting v = get(votingId);
        List<VotingOption> votingOptions = options.list("voting", v);
        Map<VotingOption, Long> counts = votingOptions.stream()
                .collect(Collectors.toMap(o -> o, votes::countByOption));
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        return votingOptions.stream()
                .map(o -> {
                    long count = counts.get(o);
                    int percent = total == 0 ? 0 : Math.round(100f * count / total);
                    return new OptionTally(o, count, percent, count > 0 && count == max);
                })
                .sorted(Comparator.comparingLong(OptionTally::votes).reversed())
                .toList();
    }

    public long totalVotes(Voting voting) {
        return options.list("voting", voting).stream()
                .mapToLong(votes::countByOption)
                .sum();
    }

    public long optionCount(Voting voting) {
        return options.count("voting", voting);
    }
}
