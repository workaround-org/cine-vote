package de.u.project.domain;

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
 * A movie nominated as an option in a voting. Stores a snapshot of the OMDb
 * movie data so results stay stable even if the upstream data changes.
 */
@Entity
@Table(name = "voting_option", uniqueConstraints = @UniqueConstraint(columnNames = { "voting_id", "imdbId" }))
public class VotingOption extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voting_id", nullable = false)
    public Voting voting;

    @Column(nullable = false)
    public String imdbId;

    @Column(nullable = false)
    public String title;

    public String year;

    public String genre;

    public String runtime;

    public String imdbRating;

    @Column(length = 1000)
    public String posterUrl;
}
