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

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

/**
 * A single approval vote cast for one option by one anonymous voter.
 */
@Entity
@Table(name = "vote")
public class Vote extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    public VotingOption option;

    @Column(nullable = false)
    public String voterId;

    @Column(nullable = false)
    public Instant createdAt = Instant.now();
}
