package com.circulosiete.curso.minibank.integration;


import com.circulosiete.curso.minibank.api.AmountRequest;
import com.circulosiete.curso.minibank.model.Account;
import com.circulosiete.curso.minibank.model.AccountType;
import com.circulosiete.curso.minibank.model.Money;
import com.circulosiete.curso.minibank.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AccountControllerITests {
    @Container
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    AccountRepository accounts;

    UUID accountId;

    @BeforeEach
    void setup() {
        accounts.findByAccountNumber("ACC-IT-001")
            .ifPresentOrElse(
                account -> this.accountId = account.getId(),
                () -> {
                    var acc = Account.open(
                        "ACC-IT-001",
                        AccountType.CURRENT,
                        UUID.randomUUID(),
                        Money.zero("MXN")
                    );
                    accounts.save(acc);
                    accountId = acc.getId();
                }
            );
    }

    @Test
    void credit_with_idempotencyKey_is_200_then_409_on_duplicate() throws Exception {
        var body = new AmountRequest(new java.math.BigDecimal("100.00"), "MXN");
        var json = objectMapper.writeValueAsString(body);

        var path = "/accounts/" + accountId + "/credit";

        mvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "req-abc")
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value("100.0"))
            .andExpect(jsonPath("$.currency").value("MXN"));

        // Retry con mismo Idempotency-Key
        mvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "req-abc")
                .content(json))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Idempotency conflict"));
    }

    @Test
    void missing_idempotencyKey_returns_400() throws Exception {
        var body = new AmountRequest(new java.math.BigDecimal("10.00"), "MXN");
        var json = objectMapper.writeValueAsString(body);

        mvc.perform(post("/accounts/" + accountId + "/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }
}
