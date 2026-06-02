package de.u.project.domain;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class VotingRepository implements PanacheRepositoryBase<Voting, UUID> {
}
