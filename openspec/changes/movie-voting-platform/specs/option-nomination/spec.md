## ADDED Requirements

### Requirement: Nominate a movie option without login

The system SHALL allow any user, without authentication, to add a movie as an option to an OPEN voting by selecting it from movie search results. The option MUST store a snapshot of the movie's title, year, genre, runtime, IMDb rating, IMDb ID, and poster URL.

#### Scenario: Add a new option

- **WHEN** an anonymous user selects a searched movie and submits it to an OPEN voting
- **THEN** the system creates a voting option storing the movie snapshot and shows it among the voting's options

#### Scenario: Reject nomination on a closed voting

- **WHEN** an anonymous user attempts to add an option to a CLOSED voting
- **THEN** the system rejects the request and adds no option

### Requirement: Prevent duplicate options

The system SHALL prevent the same movie (same IMDb ID) from being added more than once to a single voting.

#### Scenario: Duplicate movie rejected

- **WHEN** a user submits a movie whose IMDb ID already exists as an option in that voting
- **THEN** the system does not create a second option and informs the user it already exists

### Requirement: Display options with poster and stats

The system SHALL display each voting option with its poster image and short stats (year, genre, runtime, IMDb rating).

#### Scenario: Option card shows poster and stats

- **WHEN** a user views an OPEN voting
- **THEN** each option is rendered with its poster image and its short stats
