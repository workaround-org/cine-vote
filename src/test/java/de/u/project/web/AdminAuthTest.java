package de.u.project.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AdminAuthTest {

    @Test
    void publicHomeIsAccessibleWithoutLogin() {
        given().redirects().follow(false)
                .when().get("/")
                .then().statusCode(200);
    }

    @Test
    void adminRedirectsToLoginWhenUnauthenticated() {
        given().redirects().follow(false)
                .when().get("/admin")
                .then().statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    void loginPageIsPublic() {
        given().when().get("/login").then().statusCode(200);
    }

    @Test
    void validLoginGrantsAdminAccess() {
        String credential = given()
                .formParam("j_username", "admin")
                .formParam("j_password", "changeme")
                .redirects().follow(false)
                .when().post("/j_security_check")
                .then().statusCode(302)
                .extract().cookie("quarkus-credential");

        given().cookie("quarkus-credential", credential)
                .when().get("/admin")
                .then().statusCode(200)
                .body(containsString("Admin dashboard"));
    }

    @Test
    void invalidLoginIsRejected() {
        given()
                .formParam("j_username", "admin")
                .formParam("j_password", "wrong-password")
                .redirects().follow(false)
                .when().post("/j_security_check")
                .then().statusCode(302)
                .header("Location", containsString("/login"));
    }
}
