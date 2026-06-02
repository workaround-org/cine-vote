## ADDED Requirements

### Requirement: Cast approval votes without login

The system SHALL allow any user, without authentication, to select one or more options in an OPEN voting and submit them. Each selected option MUST receive exactly one vote from that submission.

#### Scenario: Vote for multiple options

- **WHEN** an anonymous user selects two or more options in an OPEN voting and submits
- **THEN** each selected option's vote count increases by one

#### Scenario: Submit with no selection

- **WHEN** a user submits the vote form without selecting any option
- **THEN** the system records no votes and prompts the user to select at least one option

### Requirement: Block voting on closed votings

The system SHALL reject vote submissions for a CLOSED voting.

#### Scenario: Voting closed

- **WHEN** a user submits votes for a CLOSED voting
- **THEN** the system rejects the submission and records no votes

### Requirement: Best-effort one ballot per voter

The system SHALL track an anonymous voter best-effort via a session cookie and prevent the same voter from submitting more than one ballot per voting.

#### Scenario: First ballot accepted

- **WHEN** a voter without a prior ballot for the voting submits their selections
- **THEN** the system records the votes and marks that voter as having voted in that voting

#### Scenario: Repeat ballot rejected

- **WHEN** a voter who already submitted a ballot for the voting submits again with the same session cookie
- **THEN** the system rejects the new ballot and keeps the original votes unchanged

### Requirement: Show live tally

The system SHALL display the current vote count per option to users viewing a voting.

#### Scenario: Counts visible after voting

- **WHEN** a user views a voting after votes have been cast
- **THEN** each option shows its current total vote count
