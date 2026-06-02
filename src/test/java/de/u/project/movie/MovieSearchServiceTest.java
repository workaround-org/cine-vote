package de.u.project.movie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.WebApplicationException;

@QuarkusTest
class MovieSearchServiceTest {

    @InjectMock
    @RestClient
    OmdbClient omdbClient;

    @Inject
    MovieSearchService service;

    private OmdbSearchResponse.Item item(String id, String title, String year, String poster) {
        OmdbSearchResponse.Item i = new OmdbSearchResponse.Item();
        i.imdbId = id;
        i.title = title;
        i.year = year;
        i.poster = poster;
        return i;
    }

    @Test
    void searchReturnsMappedResults() {
        OmdbSearchResponse resp = new OmdbSearchResponse();
        resp.response = "True";
        resp.search = List.of(item("tt1375666", "Inception", "2010", "http://poster"));
        when(omdbClient.search("Inception")).thenReturn(resp);

        List<MovieResult> results = service.search("Inception");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).imdbId()).isEqualTo("tt1375666");
        assertThat(results.get(0).title()).isEqualTo("Inception");
        assertThat(results.get(0).posterUrl()).isEqualTo("http://poster");
    }

    @Test
    void blankTermReturnsEmptyAndSkipsOmdb() {
        List<MovieResult> results = service.search("   ");

        assertThat(results).isEmpty();
        verify(omdbClient, never()).search(anyString());
    }

    @Test
    void noMatchesReturnsEmptyList() {
        OmdbSearchResponse resp = new OmdbSearchResponse();
        resp.response = "False";
        resp.error = "Movie not found!";
        when(omdbClient.search(anyString())).thenReturn(resp);

        assertThat(service.search("zzzznotarealmovie")).isEmpty();
    }

    @Test
    void omdbFailureRaisesMovieSearchException() {
        when(omdbClient.search(anyString())).thenThrow(new WebApplicationException(500));

        assertThatThrownBy(() -> service.search("Inception"))
                .isInstanceOf(MovieSearchException.class);
    }

    @Test
    void getDetailMapsFieldsAndNormalizesNotAvailable() {
        OmdbMovieResponse resp = new OmdbMovieResponse();
        resp.response = "True";
        resp.imdbId = "tt1375666";
        resp.title = "Inception";
        resp.year = "2010";
        resp.genre = "Action, Sci-Fi";
        resp.runtime = "148 min";
        resp.imdbRating = "8.8";
        resp.poster = "N/A";
        when(omdbClient.findById("tt1375666")).thenReturn(resp);

        MovieDetail detail = service.getDetail("tt1375666");

        assertThat(detail.title()).isEqualTo("Inception");
        assertThat(detail.genre()).isEqualTo("Action, Sci-Fi");
        assertThat(detail.imdbRating()).isEqualTo("8.8");
        assertThat(detail.posterUrl()).isNull();
    }

    @Test
    void getDetailNotFoundRaisesException() {
        OmdbMovieResponse resp = new OmdbMovieResponse();
        resp.response = "False";
        resp.error = "Incorrect IMDb ID.";
        when(omdbClient.findById(anyString())).thenReturn(resp);

        assertThatThrownBy(() -> service.getDetail("ttbad"))
                .isInstanceOf(MovieSearchException.class);
    }
}
