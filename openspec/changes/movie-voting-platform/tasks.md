## 1. Project setup & cleanup

- [x] 1.1 Add dependencies: `quarkus-security-jpa`, `quarkus-elytron-security-common`, `quarkus-rest-client-jackson` to `pom.xml`
- [x] 1.2 Remove demo scaffold: `GreetingResource`, `MyEntity`, `SomePage`, `page.qute.html`, demo tests, `import.sql` demo rows
- [x] 1.3 Add config keys to `application.properties`: `omdb.api-key`, OMDb base URL, admin bootstrap username/password, Hibernate schema strategy per env
- [x] 1.4 Update `README.md`: purpose, OMDb key setup, admin bootstrap, run/dev instructions

## 2. Data model

- [x] 2.1 Write failing tests for entity persistence/constraints (`Voting`, `VotingOption` unique `(voting,imdbId)`, `Vote`, `Ballot` unique `(voting,voterId)`)
- [x] 2.2 Create `Voting` entity with `state` (OPEN/CLOSED), timestamps (`WithId.AutoUUID`)
- [x] 2.3 Create `VotingOption` entity with movie snapshot fields + unique constraint
- [x] 2.4 Create `Vote` and `Ballot` entities with constraints
- [x] 2.5 Make entity tests green

## 3. OMDb movie search (capability: movie-search)

- [x] 3.1 Write failing tests (WireMock/mock) for search list, detail fetch, empty term, no-match, OMDb failure
- [x] 3.2 Define `@RegisterRestClient` OMDb client interface (`?s=` search, `?i=` detail) with API key
- [x] 3.3 Implement `MovieSearchService`: map OMDb responses to internal DTOs (title, year, genre, runtime, imdbRating, imdbId, posterUrl)
- [x] 3.4 Handle failures gracefully (missing key, timeout, rate limit) — no persistence on error
- [x] 3.5 Make movie-search tests green

## 4. Admin authentication (capability: admin-auth)

- [x] 4.1 Write failing tests: login success/failure, protected-route blocked when unauthenticated, logout invalidates session
- [x] 4.2 Create `AdminUser` entity with `@UserDefinition` (`@Username`, `@Password` bcrypt, `@Roles`)
- [x] 4.3 Bootstrap admin at startup from config when table empty (hash with `BcryptUtil`)
- [x] 4.4 Configure form-based auth + HTTP security policy: `/admin/**` requires role `admin`
- [x] 4.5 Implement `/login`, `/logout` endpoints + login Qute template
- [x] 4.6 Make admin-auth tests green

## 5. Voting management — admin (capability: voting-management)

- [x] 5.1 Write failing tests: create (valid + blank-title reject), list with state/counts, end open voting, reject ending closed, results ranked
- [x] 5.2 Implement `VotingService`: create, list, end (OPEN→CLOSED guard), results ordered by vote count
- [x] 5.3 Implement admin endpoints: `GET /admin`, `POST /admin/votings`, `POST /admin/votings/{id}/close`
- [x] 5.4 Create admin Qute templates: dashboard list + create-voting form
- [x] 5.5 Make voting-management tests green

## 6. Option nomination — public (capability: option-nomination)

- [x] 6.1 Write failing tests: add option to open voting, reject on closed voting, reject duplicate imdbId, snapshot stored
- [x] 6.2 Implement nominate flow: `GET /search?q=` (OMDb search fragment), `POST /votings/{id}/options` storing snapshot
- [x] 6.3 Enforce OPEN-state + duplicate-imdbId guards
- [x] 6.4 Render option cards with poster + short stats in voting template
- [x] 6.5 Make option-nomination tests green

## 7. Vote casting — public (capability: vote-casting)

- [x] 7.1 Write failing tests: multi-option ballot increments each, empty selection rejected, closed voting rejected, first ballot accepted, repeat ballot (same cookie) rejected, tally visible
- [x] 7.2 Implement `voter_id` HTTP-only cookie issuance on first ballot
- [x] 7.3 Implement `POST /votings/{id}/votes`: approval tally + `Ballot` dedup `(voting,voterId)`
- [x] 7.4 Render multi-select vote form + live per-option tally
- [x] 7.5 Make vote-casting tests green

## 8. Public UI & polish

- [x] 8.1 Implement `GET /` public list of votings (open, plus read-only closed results) with shared Qute `layout`
- [x] 8.2 Implement `GET /votings/{id}` combining options, nominate, and vote sections
- [x] 8.3 Basic styling for poster grid / stat display
- [x] 8.4 Verify health endpoints still serve; smoke-test full flow in dev mode

## 9. Verification & release

- [x] 9.1 Run full test suite via bishbash; all green
- [ ] 9.2 Format code: `mvn formatter:format` + `mvn impsort:sort` (add plugins if absent)
- [ ] 9.3 Pre-commit README check, then commit-gatekeeper review before commit
