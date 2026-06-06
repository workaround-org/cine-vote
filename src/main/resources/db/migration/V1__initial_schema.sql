-- Initial schema. Matches the Hibernate entity model exactly (verified via
-- quarkus.hibernate-orm.schema-management.strategy=validate in dev/test):
-- Quarkus keeps Hibernate's default naming, so camelCase fields stay camelCase
-- columns, and Instant maps to TIMESTAMP(6) WITH TIME ZONE.

CREATE SEQUENCE IF NOT EXISTS app_user_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE app_user
(
    id       BIGINT NOT NULL,
    username VARCHAR(255),
    password VARCHAR(255),
    role     VARCHAR(255),
    CONSTRAINT pk_app_user PRIMARY KEY (id)
);

CREATE TABLE voting
(
    id          UUID         NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    state       VARCHAR(255) NOT NULL,
    createdAt   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    closedAt    TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT pk_voting PRIMARY KEY (id),
    CONSTRAINT ck_voting_state CHECK (state IN ('OPEN', 'CLOSED'))
);

CREATE TABLE voting_option
(
    id         UUID         NOT NULL,
    voting_id  UUID         NOT NULL,
    imdbId     VARCHAR(255) NOT NULL,
    title      VARCHAR(255) NOT NULL,
    year       VARCHAR(255),
    genre      VARCHAR(255),
    runtime    VARCHAR(255),
    imdbRating VARCHAR(255),
    posterUrl  VARCHAR(1000),
    CONSTRAINT pk_voting_option PRIMARY KEY (id)
);

CREATE TABLE ballot
(
    id        UUID         NOT NULL,
    voting_id UUID         NOT NULL,
    voterId   VARCHAR(255) NOT NULL,
    createdAt TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_ballot PRIMARY KEY (id)
);

CREATE TABLE vote
(
    id        UUID         NOT NULL,
    option_id UUID         NOT NULL,
    voterId   VARCHAR(255) NOT NULL,
    createdAt TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_vote PRIMARY KEY (id)
);

ALTER TABLE voting_option
    ADD CONSTRAINT uc_voting_option_voting_imdb UNIQUE (voting_id, imdbId);

ALTER TABLE ballot
    ADD CONSTRAINT uc_ballot_voting_voter UNIQUE (voting_id, voterId);

ALTER TABLE voting_option
    ADD CONSTRAINT fk_voting_option_on_voting FOREIGN KEY (voting_id) REFERENCES voting (id);

ALTER TABLE ballot
    ADD CONSTRAINT fk_ballot_on_voting FOREIGN KEY (voting_id) REFERENCES voting (id);

ALTER TABLE vote
    ADD CONSTRAINT fk_vote_on_option FOREIGN KEY (option_id) REFERENCES voting_option (id);
