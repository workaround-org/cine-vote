package de.u.project.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class HealthCheckTest {

    @Test
    void healthEndpointIsUpAndPublic() {
        given().redirects().follow(false)
                .when().get("/q/health")
                .then().statusCode(200)
                .body("status", is("UP"));
    }
}
