## ADDED Requirements

### Requirement: Search movies via OMDb

The system SHALL search movies by title against the OMDb API and return a list of matches, each with title, year, IMDb ID, and poster URL.

#### Scenario: Search returns matches

- **WHEN** a user submits a non-empty search term
- **THEN** the system queries OMDb and returns the matching movies with poster and year

#### Scenario: Empty search term

- **WHEN** a user submits a blank search term
- **THEN** the system returns no results and does not call OMDb

#### Scenario: No matches found

- **WHEN** OMDb returns no results for the search term
- **THEN** the system shows an empty result set with a "no matches" message

### Requirement: Retrieve movie stats

The system SHALL retrieve, for a selected movie (by IMDb ID), its short stats: title, year, genre, runtime, IMDb rating, and poster URL.

#### Scenario: Fetch full stats for a movie

- **WHEN** a user selects a movie from search results
- **THEN** the system fetches its detail from OMDb and exposes title, year, genre, runtime, IMDb rating, and poster URL

### Requirement: Handle OMDb failures gracefully

The system SHALL handle OMDb errors (missing/invalid API key, network failure, rate limit) without crashing the request.

#### Scenario: OMDb unavailable

- **WHEN** an OMDb request fails or times out
- **THEN** the system shows a friendly error and allows the user to retry, persisting nothing
