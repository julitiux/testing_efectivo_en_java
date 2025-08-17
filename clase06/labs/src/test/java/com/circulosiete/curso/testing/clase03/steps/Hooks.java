package com.circulosiete.curso.testing.clase03.steps;

import com.circulosiete.curso.testing.clase03.TransferService;
import com.circulosiete.curso.testing.clase03.adapters.jdbc.JdbcAccountRepository;
import com.circulosiete.curso.testing.clase03.service.Notifier;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;

public class Hooks {
    public static final Notifier NOOP_NOTIFIER = (accountId, message) -> {

    };
    private static TransferContext transferContext;
    private static PostgreSQLContainer<?> postgres;

    public static TransferContext transferContext() {
        return transferContext;
    }

    public Hooks() {
        transferContext = new TransferContext();
    }

    @Before
    public void startPerScenario() {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("test")
                .withUsername("test")
                .withPassword("test");
        postgres.start();

        var config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());

        var dataSource = new HikariDataSource(config);

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        var accountRepository = new JdbcAccountRepository(dataSource);
        var transferService = new TransferService(
                NOOP_NOTIFIER, accountRepository
        );

        transferContext.setDataSource(dataSource);
        transferContext.setAccountRepository(accountRepository);
        transferContext.setTransferService(transferService);
    }

    @After
    public void stopPerScenario() {
        if (postgres != null) {
            postgres.stop();
        }
    }
}
