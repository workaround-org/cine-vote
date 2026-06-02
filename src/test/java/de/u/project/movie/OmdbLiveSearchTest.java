package de.u.project.movie;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Opt-in integration test that hits the real OMDb API. It runs only when an
 * {@code OMDB_API_KEY} environment variable is present, so CI runs and developers
 * without a key skip it cleanly. Unlike the mocked unit tests, this exercises the
 * full network path — the real {@link OmdbClient}, key sanitization, and OMDb's
 * actual responses — which is what catches issues like a malformed request URL
 * (e.g. a stray CR in the key triggering an upstream Cloudflare 400).
 */
@QuarkusTest
@EnabledIfEnvironmentVariable(named = "OMDB_API_KEY", matches = ".+")
class OmdbLiveSearchTest {

    @Inject
    MovieSearchService service;

    @Test
    void liveSearchReturnsResults() {
        List<MovieResult> results = service.search("batman");

        assertThat(results)
                .as("real OMDb search for 'batman' should return at least one match")
                .isNotEmpty();
        assertThat(results.get(0).title()).isNotBlank();
        assertThat(results.get(0).imdbId()).startsWith("tt");
    }

    @Test
    void liveDetailLookupReturnsStats() {
        // tt1375666 = Inception, a stable, always-present OMDb entry.
        MovieDetail detail = service.getDetail("tt1375666");

        assertThat(detail.title()).isEqualTo("Inception");
        assertThat(detail.year()).isEqualTo("2010");
        assertThat(detail.imdbRating()).isNotBlank();
    }
}
