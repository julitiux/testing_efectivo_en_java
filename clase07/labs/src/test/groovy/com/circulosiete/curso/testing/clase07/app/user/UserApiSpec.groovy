package com.circulosiete.curso.testing.clase07.app.user

import io.restassured.RestAssured
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.spock.Testcontainers
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
//@org.springframework.test.context.ActiveProfiles("test")(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(
    "test"
)
@Testcontainers
@Ignore
class UserApiSpec extends Specification {
    @Shared
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16");

    @LocalServerPort
    int port;

    def setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    def "create and fetch user through REST API"() {
        when: "we create a user"
            def id = RestAssured.given()
                .contentType("application/json")
                .body([name: "carol"])
                .post("/users")
                .then()
                .statusCode(200)
                .extract().jsonPath().getLong("id")

        then:
            id != null

        when: "we fetch it"
            def name = RestAssured.given()
                .get("/users/{id}", id)
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("name")

        then:
            name == "carols"
    }
}
