package com.circulosiete.curso.testing.clase03;

import com.circulosiete.curso.testing.clase03.adapters.jdbc.JdbcAccountRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class JdbcAccountRepositoryIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:16");

    private static DataSource dataSource;
    private static JdbcAccountRepository jdbcAccountRepository;

    @BeforeAll
    public static void setUp() {
        postgreSQLContainer.start();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
        hikariConfig.setUsername(postgreSQLContainer.getUsername());
        hikariConfig.setPassword(postgreSQLContainer.getPassword());
        dataSource = new HikariDataSource(hikariConfig);

        // migro mi base de datos
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
        jdbcAccountRepository = new JdbcAccountRepository(dataSource);
    }

    @Test
    void save() {
        UUID accountId = UUID.randomUUID();
        var foundAccount = jdbcAccountRepository.findById(accountId);
        assertTrue(foundAccount.isEmpty());
        var account = new Account(accountId, BigDecimal.ZERO);
        jdbcAccountRepository.save(account);
        foundAccount = jdbcAccountRepository.findById(account.getUuid());
        assertTrue(foundAccount.isPresent());
    }
}
