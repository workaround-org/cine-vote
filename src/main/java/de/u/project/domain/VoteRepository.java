package de.u.project.domain;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class VoteRepository implements PanacheRepositoryBase<Vote, UUID> {

    public long countByOption(VotingOption option) {
        return count("option", option);
    }
}
