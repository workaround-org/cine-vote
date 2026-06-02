## Why

There is no lightweight way for a group to collectively decide which movie to watch. Admins need to run time-boxed votings, and participants need a frictionless way to nominate and vote on movies without creating accounts. Pulling poster and rating data from IMDb (via OMDb) makes options recognizable at a glance.

## What Changes

- Add an **admin UI** (login-protected) to create votings, list them, and end (close) a voting.
- Add a **public voting UI** (no login) where anyone can view an open voting, nominate new movie options, and cast votes.
- Add **movie search** against the OMDb API: search by title, pick a result, and store poster URL + short stats (year, genre, IMDb rating, runtime) with each option.
- Support **approval voting**: a participant may select more than one option in a single voting; each selected option receives one vote.
- Track anonymous voters **best-effort by a session cookie** to discourage trivial double-voting, while keeping voting login-free.
- Add **admin authentication** with username + password credentials stored (hashed) in the database.

## Capabilities

### New Capabilities

- `admin-auth`: Database-backed username/password authentication and session for admin-only actions.
- `voting-management`: Admin-side lifecycle of a voting — create, list, view results, and end (close) a voting.
- `movie-search`: Search OMDb for movies and retrieve poster + short stats for use as voting options.
- `option-nomination`: Public, login-free nomination of movie options into an open voting, backed by a movie search result.
- `vote-casting`: Public, login-free approval voting — selecting one or more options in an open voting and tallying results.

### Modified Capabilities

<!-- None — greenfield project, no existing specs. -->

## Impact

- **New dependencies**: `quarkus-security-jpa` (or equivalent) for admin auth, `quarkus-rest-client-jackson` for OMDb HTTP calls.
- **Config**: OMDb API key (`omdb.api-key`), OMDb base URL, admin bootstrap credentials.
- **Data model**: new entities — `AdminUser`, `Voting`, `VotingOption` (movie snapshot), `Vote`.
- **APIs/UI**: new Qute-rendered pages (admin + public) and supporting REST/form endpoints.
- **External**: outbound calls to `https://www.omdbapi.com/`; requires network access and a valid API key.
- **Existing scaffold**: greeting/demo classes (`GreetingResource`, `MyEntity`, `SomePage`, `page.qute.html`) to be removed or replaced.
