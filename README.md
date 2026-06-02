# cine-vote

A lightweight movie-voting platform built with Quarkus. Admins create time-boxed
votings; anyone can nominate movies and cast votes without logging in. Movie
metadata (poster + short stats) is pulled from the [OMDb API](https://www.omdbapi.com/).

## Features

- **Admin UI** (login-protected): create votings, list them, view results, and end (close) a voting.
- **Public UI** (no login): view open votings, search OMDb for movies, nominate options, and cast votes.
- **Approval voting**: a participant may select more than one option; each selected option gets one vote.
- **Best-effort one ballot per voter** via an HTTP-only session cookie.
- Each option stores a snapshot of the movie (title, year, genre, runtime, IMDb rating, poster URL) so results stay stable.
- OMDb responses are cached in-memory (Caffeine) to reduce external API calls; the snapshot + cache together mean repeated lookups almost never hit the OMDb API.

## Configuration

| Property | Env var | Default | Purpose |
|---|---|---|---|
| `cinevote.omdb.api-key` | `OMDB_API_KEY` | `demo` | OMDb API key — get a free one at [omdbapi.com/apikey.aspx](https://www.omdbapi.com/apikey.aspx) |
| `quarkus.rest-client.omdb.url` | — | `https://www.omdbapi.com` | OMDb base URL |
| `cinevote.admin.bootstrap-username` | `ADMIN_USERNAME` | `admin` | Initial admin username (created at startup if no admin exists) |
| `cinevote.admin.bootstrap-password` | `ADMIN_PASSWORD` | `changeme` | Initial admin password (stored bcrypt-hashed) |
| `quarkus.http.auth.session.encryption-key` | `SESSION_ENCRYPTION_KEY` | dev placeholder | Form-auth session cookie key (≥ 32 chars) — **set a real secret in prod** |
| `quarkus.cache.caffeine."omdb-search".expire-after-write` | — | `6H` | TTL for cached movie search results |
| `quarkus.cache.caffeine."omdb-search".maximum-size` | — | `500` | Max number of cached search terms |
| `quarkus.cache.caffeine."omdb-detail".expire-after-write` | — | `24H` | TTL for cached movie detail lookups |
| `quarkus.cache.caffeine."omdb-detail".maximum-size` | — | `1000` | Max number of cached movie details |

The admin user is bootstrapped on first startup from the config above. Change the
default password before exposing the app.

## Running in dev mode

```shell
./mvnw quarkus:dev
```

Dev mode auto-starts a PostgreSQL container (Dev Services) and uses the `demo`
OMDb key by default — set `OMDB_API_KEY` to exercise real search. Dev UI:
<http://localhost:8080/q/dev/>.

- Public site: <http://localhost:8080/>
- Admin: <http://localhost:8080/admin> (redirects to `/login`)

## Testing

```shell
./mvnw test
```

Tests run against an ephemeral PostgreSQL (Dev Services, requires Docker) and mock
the OMDb REST client, so no network or real API key is needed.

## Packaging and running

```shell
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

Native build:

```shell
./mvnw package -Dnative
```

## Tech stack

Quarkus 3.36 (Java 25), Qute server-side templates, Hibernate ORM with Panache
(PostgreSQL), `quarkus-security-jpa` form authentication, and `quarkus-rest-client`
for OMDb.
