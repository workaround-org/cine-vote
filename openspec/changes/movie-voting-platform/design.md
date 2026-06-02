## Context

`cine-vote` is a fresh Quarkus 3.36 project (Java 25) with the scaffold already including `quarkus-rest`, `quarkus-rest-qute`, `quarkus-rest-jackson`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-smallrye-openapi`, and `quarkus-smallrye-health`. Only demo classes exist (`GreetingResource`, `MyEntity`, `SomePage`, `page.qute.html`).

The platform serves two audiences over one Quarkus app:
- **Admins** (authenticated): manage voting lifecycle.
- **Anonymous users** (no login): nominate movies and cast approval votes.

Movie metadata comes from the **OMDb API** (free key), since IMDb has no free public API. UI is **server-side rendered with Qute** (already in the stack) — no separate SPA. Admin auth uses **username/password stored in Postgres**. Voting is **approval voting** (select many, each gets +1).

## Goals / Non-Goals

**Goals:**
- Login-protected admin UI to create, list, view, and end votings.
- Login-free public UI to view an open voting, search OMDb, nominate options, and cast multi-select votes.
- Persist a movie snapshot (poster + short stats) per option so results stay stable even if OMDb changes.
- Best-effort one-ballot-per-voter via session cookie.
- Graceful degradation when OMDb is unavailable.

**Non-Goals:**
- No user accounts/profiles for voters (anonymous only).
- No ranked-choice or weighted voting.
- No real-time push updates (page reload reflects new tallies).
- No multi-tenant or org separation.
- No bullet-proof anti-fraud (cookie dedup is best-effort, not adversarial).

## Decisions

### Movie data: OMDb via Quarkus REST Client
Use a declarative `@RegisterRestClient` interface against `https://www.omdbapi.com/`. Two calls: `?s=<title>` (search list) and `?i=<imdbID>` (full detail with Genre/Runtime/imdbRating). API key injected from config `omdb.api-key`.
- *Alternatives*: TMDb (richer but not IMDb-branded), scraping imdb.com (fragile, ToS risk). OMDb chosen for IMDb-native fields and simplicity.

### Movie snapshot stored on the option, not referenced live
`VotingOption` stores title, year, genre, runtime, imdbRating, imdbId, posterUrl at nomination time. Avoids repeated OMDb calls on every render, keeps results reproducible, and survives OMDb outages.

### Admin auth: quarkus-security-jpa + form authentication
Add `quarkus-security-jpa` and `quarkus-elytron-security-common` for `BcryptUtil` password hashing. `AdminUser` entity maps `@UserDefinition` (`@Username`, `@Password`, `@Roles`). Form-based login backed by Quarkus HTTP security policy; admin routes require role `admin`. Bootstrap admin created at startup from config if the table is empty.
- *Alternatives*: static shared token (less auditable), OIDC/Keycloak (overkill for scope).

### Approval voting model
On submit, the public ballot form posts a set of option IDs. Each selected option gets one `Vote` row (or an incremented counter). A `Ballot` record per voter+voting enforces single submission.

### Anonymous voter identity: HTTP-only session cookie
On first ballot, issue a random UUID cookie (`voter_id`). A unique constraint `(voting_id, voter_id)` blocks repeat ballots. Best-effort only — cleared cookies bypass it, which is acceptable.

### Data model
- `AdminUser(id, username, passwordHash, role)`
- `Voting(id, title, description, state[OPEN|CLOSED], createdAt, closedAt)`
- `VotingOption(id, voting_id, imdbId, title, year, genre, runtime, imdbRating, posterUrl)` — unique `(voting_id, imdbId)`
- `Vote(id, option_id, voterId, createdAt)` — supports per-option tally
- `Ballot(id, voting_id, voterId, createdAt)` — unique `(voting_id, voterId)`

IDs use `WithId.AutoUUID` (Panache Next preference). Tally = count of `Vote` per option.

### Routing / UI structure
- Public: `GET /` (list open votings), `GET /votings/{id}` (view + nominate + vote), `POST /votings/{id}/options` (nominate), `POST /votings/{id}/votes` (ballot), `GET /search?q=` (OMDb search fragment).
- Admin: `GET /admin` (list), `POST /admin/votings` (create), `POST /admin/votings/{id}/close` (end), `GET /login` / `POST /login` / `POST /logout`.
- Qute templates: `layout`, public `index`/`voting`, admin `dashboard`/`voting-form`, `login`.

### Testing approach
Per global rule (test-first), each scenario maps to a `@QuarkusTest` with RestAssured. OMDb client mocked via `@InjectMock` / WireMock so tests don't hit the network. Postgres via Quarkus Dev Services (Testcontainers) in tests.

## Risks / Trade-offs

- **OMDb rate limit / key exhaustion (1000/day free)** → cache search results briefly; store snapshot so detail is fetched once per option.
- **OMDb downtime blocks nomination** → search/detail failures show retry UI and persist nothing; existing options/votes unaffected.
- **Cookie-based dedup is trivially bypassable** → accepted as best-effort per requirements; documented, not a security control.
- **No login for voting invites spam/duplicate options** → duplicate-by-imdbId guard + admin can close voting; further abuse out of scope.
- **Server-side Qute over SPA** → less dynamic UX, but matches stack and keeps the app single-deployable; full-page reloads acceptable for this scale.
- **Storing poster URLs (hotlinked from OMDb/Amazon)** → images may break if upstream removes them; acceptable, no local mirroring planned.

## Migration Plan

Greenfield — no data migration. Deploy as a single Quarkus app + Postgres. Remove demo scaffold classes. Schema generated by Hibernate (`drop-and-create` in dev, `update`/validate in prod). Rollback = redeploy previous image; DB is additive. Requires `omdb.api-key` and admin bootstrap config present before first start.

## Open Questions

- Should closed votings remain publicly visible (read-only results) or be hidden from the public list? *Assumed: visible, read-only.*
- Cap on number of options per voting to limit spam? *Assumed: no hard cap for now.*
