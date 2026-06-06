package de.u.project.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class FaviconTest {

    @Test
    void faviconIsServed() {
        given().when().get("/favicon.svg")
                .then().statusCode(200)
                .contentType(containsString("svg"));
    }

    @Test
    void pagesLinkTheFavicon() {
        given().when().get("/")
                .then().statusCode(200)
                .body(containsString("rel=\"icon\""));
    }
}
