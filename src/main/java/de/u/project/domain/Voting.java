package de.u.project.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.qute.TemplateData;

@Entity
@Table(name = "voting")
@TemplateData
public class Voting extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String title;

    @Column(length = 2000)
    public String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public VotingState state = VotingState.OPEN;

    @Column(nullable = false)
    public Instant createdAt = Instant.now();

    public Instant closedAt;

    public boolean isOpen() {
        return state == VotingState.OPEN;
    }
}
