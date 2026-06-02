package de.u.project.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

/**
 * Records that a given anonymous voter has submitted a ballot for a voting.
 * Enforces best-effort one-ballot-per-voter via the unique (voting, voterId) constraint.
 */
@Entity
@Table(name = "ballot", uniqueConstraints = @UniqueConstraint(columnNames = { "voting_id", "voterId" }))
public class Ballot extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voting_id", nullable = false)
    public Voting voting;

    @Column(nullable = false)
    public String voterId;

    @Column(nullable = false)
    public Instant createdAt = Instant.now();
}
