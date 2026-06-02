package de.u.project.domain;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class BallotRepository implements PanacheRepositoryBase<Ballot, UUID> {

    public boolean hasVoted(Voting voting, String voterId) {
        return count("voting = ?1 and voterId = ?2", voting, voterId) > 0;
    }
}
