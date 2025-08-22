package com.circulosiete.curso.minibank.bdd;

import com.circulosiete.curso.minibank.MiniBankApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.basePath;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.port;

@CucumberContextConfiguration
@SpringBootTest(
    classes = {
        MiniBankApplication.class,
        CucumberSpringConfig.TestContainersCfg.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
public class CucumberSpringConfig {

    @TestConfiguration(proxyBeanMethods = false)
    static class TestContainersCfg {
        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgres() {
            // NO llames .start(): Spring Boot lo maneja
            return new PostgreSQLContainer<>("postgres:16");
        }
    }

    @LocalServerPort
    void setPort(int appPort) {
        baseURI = "http://localhost";
        basePath = "";
        port = appPort;
    }
}
