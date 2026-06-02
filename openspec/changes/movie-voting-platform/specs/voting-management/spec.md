## ADDED Requirements

### Requirement: Create a voting

The system SHALL allow an authenticated admin to create a voting with a title and an optional description. A new voting MUST start in the OPEN state.

#### Scenario: Create voting with title

- **WHEN** an admin submits a create-voting form with a non-empty title
- **THEN** the system persists a new voting in the OPEN state and shows it in the admin list

#### Scenario: Reject empty title

- **WHEN** an admin submits a create-voting form with a blank title
- **THEN** the system rejects the request with a validation error and creates no voting

### Requirement: List votings

The system SHALL show an authenticated admin a list of all votings with their title, state (OPEN/CLOSED), and option/vote counts.

#### Scenario: List shows state and counts

- **WHEN** an admin opens the votings list
- **THEN** each voting row displays its title, current state, number of options, and total votes

### Requirement: End a voting

The system SHALL allow an authenticated admin to end (close) an OPEN voting. Once CLOSED, the voting MUST NOT accept new options or votes.

#### Scenario: Close an open voting

- **WHEN** an admin ends an OPEN voting
- **THEN** the voting transitions to CLOSED and its results become final

#### Scenario: Ending an already-closed voting is rejected

- **WHEN** an admin attempts to end a voting that is already CLOSED
- **THEN** the system rejects the action and leaves the voting unchanged

### Requirement: View voting results

The system SHALL display the options of a voting ranked by vote count, for both OPEN and CLOSED votings.

#### Scenario: Results ranked by votes

- **WHEN** an admin views a voting's results
- **THEN** options are listed in descending order of vote count with each option's total
