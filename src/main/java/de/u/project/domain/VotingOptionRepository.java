package de.u.project.domain;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class VotingOptionRepository implements PanacheRepositoryBase<VotingOption, UUID> {

    public boolean existsByVotingAndImdbId(Voting voting, String imdbId) {
        return count("voting = ?1 and imdbId = ?2", voting, imdbId) > 0;
    }

    public Optional<VotingOption> findByVotingAndImdbId(Voting voting, String imdbId) {
        return find("voting = ?1 and imdbId = ?2", voting, imdbId).firstResultOptional();
    }
}
