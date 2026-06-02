package de.u.project.movie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.cache.CacheManager;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

/**
 * Verifies the OMDb response cache: a repeated lookup with the same key hits the
 * external client only once. Caching is disabled in the default test profile (so
 * {@link MovieSearchServiceTest} stays deterministic), so this test re-enables it
 * via a dedicated profile.
 */
@QuarkusTest
@TestProfile(MovieSearchCacheTest.CacheEnabledProfile.class)
class MovieSearchCacheTest {

    public static class CacheEnabledProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("quarkus.cache.enabled", "true");
        }
    }

    @InjectMock
    @RestClient
    OmdbClient omdbClient;

    @Inject
    MovieSearchService service;

    @Inject
    CacheManager cacheManager;

    /** Caches are process-wide and survive between test methods; clear them so each test is isolated. */
    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames()
                .forEach(name -> cacheManager.getCache(name)
                        .ifPresent(cache -> cache.invalidateAll().await().indefinitely()));
    }

    @Test
    void repeatedSearchHitsOmdbOnce() {
        OmdbSearchResponse resp = new OmdbSearchResponse();
        resp.response = "True";
        OmdbSearchResponse.Item i = new OmdbSearchResponse.Item();
        i.imdbId = "tt1375666";
        i.title = "Inception";
        i.year = "2010";
        i.poster = "http://poster";
        resp.search = List.of(i);
        when(omdbClient.search(anyString(), eq("Inception"))).thenReturn(resp);

        List<MovieResult> first = service.search("Inception");
        List<MovieResult> second = service.search("Inception");

        assertThat(first).hasSize(1);
        assertThat(second).isEqualTo(first);
        verify(omdbClient, times(1)).search(anyString(), eq("Inception"));
    }

    @Test
    void repeatedDetailHitsOmdbOnce() {
        OmdbMovieResponse resp = new OmdbMovieResponse();
        resp.response = "True";
        resp.imdbId = "tt1375666";
        resp.title = "Inception";
        resp.year = "2010";
        resp.genre = "Action, Sci-Fi";
        resp.runtime = "148 min";
        resp.imdbRating = "8.8";
        resp.poster = "http://poster";
        when(omdbClient.findById(anyString(), eq("tt1375666"))).thenReturn(resp);

        MovieDetail first = service.getDetail("tt1375666");
        MovieDetail second = service.getDetail("tt1375666");

        assertThat(second).isEqualTo(first);
        verify(omdbClient, times(1)).findById(anyString(), eq("tt1375666"));
    }

    @Test
    void distinctTermsEachHitOmdb() {
        OmdbSearchResponse resp = new OmdbSearchResponse();
        resp.response = "False";
        resp.error = "Movie not found!";
        when(omdbClient.search(anyString(), eq("Inception"))).thenReturn(resp);
        when(omdbClient.search(anyString(), eq("Matrix"))).thenReturn(resp);

        service.search("Inception");
        service.search("Matrix");

        verify(omdbClient, times(1)).search(anyString(), eq("Inception"));
        verify(omdbClient, times(1)).search(anyString(), eq("Matrix"));
    }
}
