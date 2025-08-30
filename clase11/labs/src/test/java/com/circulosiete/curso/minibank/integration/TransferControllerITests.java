package com.circulosiete.curso.minibank.integration;

import com.circulosiete.curso.minibank.api.TransferRequest;
import com.circulosiete.curso.minibank.model.Account;
import com.circulosiete.curso.minibank.model.AccountType;
import com.circulosiete.curso.minibank.model.Money;
import com.circulosiete.curso.minibank.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class TransferControllerITests {
    @Container
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    AccountRepository accounts;

    UUID accFromMx;
    UUID accToMx;
    UUID accUsd; // para pruebas de moneda

    Account createAccount(Account account) {
        return accounts
            .findByAccountNumber(account.getAccountNumber())
            .orElseGet(() -> accounts.save(account));
    }

    @BeforeEach
    void setup() {

        var from = Account.open("ACC-FROM-MXN", AccountType.CURRENT, UUID.randomUUID(), Money.of(new BigDecimal("1000.00"), "MXN"));
        var to = Account.open("ACC-TO-MXN", AccountType.CURRENT, UUID.randomUUID(), Money.of(new BigDecimal("100.00"), "MXN"));
        var usd = Account.open("ACC-USD", AccountType.CURRENT, UUID.randomUUID(), Money.of(new BigDecimal("0.00"), "USD"));

//        createAccount(from);
//        createAccount(to);
//        createAccount(usd);

        accFromMx = createAccount(from).getId();
        accToMx = createAccount(to).getId();
        accUsd = createAccount(usd).getId();
        System.out.println("Accounts: accFromMx " + accFromMx + ", accToMx " + accToMx + ", accUsd " + accUsd);
    }

    @Test
    void transfer_happy_path_returns_200_and_updates_balances() throws Exception {
        var body = new TransferRequest(accFromMx, accToMx, new BigDecimal("250.00"), "MXN");
        System.out.println("Record: " + body);
        var json = objectMapper.writeValueAsString(body);
        System.out.println("JSON: " + json);

        mvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "xfer-1")
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.amount").value(250.00))
            .andExpect(jsonPath("$.currency").value("MXN"))
            .andExpect(jsonPath("$.from.balance").value(750.00))   // 1000 - 250
            .andExpect(jsonPath("$.to.balance").value(350.00));    // 100 + 250

        var from = accounts.findById(accFromMx).orElseThrow();
        var to = accounts.findById(accToMx).orElseThrow();
        assertThat(from.getBalance().getAmount()).isEqualByComparingTo("750.00");
        assertThat(to.getBalance().getAmount()).isEqualByComparingTo("350.00");
    }

    @Test
    void transfer_is_idempotent_by_requestId_second_call_returns_409() throws Exception {
        accounts.findById(accFromMx)
            .ifPresent(account -> {
                account.setBalance(Money.of(new BigDecimal("1000.00"), "MXN"));
                accounts.save(account);
            });
        accounts.findById(accToMx)
            .ifPresent(account -> {
                account.setBalance(Money.of(new BigDecimal("100.00"), "MXN"));
                accounts.save(account);
            });
        BigDecimal amountToTransfer = new BigDecimal("100.00");
        var body = new TransferRequest(accFromMx, accToMx, amountToTransfer, "MXN");
        var json = objectMapper.writeValueAsString(body);

        // Primera vez OK
        mvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "xfer-dup")
                .content(json))
            .andExpect(status().isOk());

        // Mismo requestId => 409
        mvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "xfer-dup")
                .content(json))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Idempotency conflict"));

        var from = accounts.findById(accFromMx).orElseThrow();
        var to = accounts.findById(accToMx).orElseThrow();
        // saldo movido solo una vez
        assertThat(from.getBalance().getAmount()).isEqualByComparingTo("900.00"); // 1000 - 100
        assertThat(to.getBalance().getAmount()).isEqualByComparingTo("200.00");   // 100 + 100
    }

    @Test
    void transfer_insufficient_funds_returns_422_and_no_balance_change() throws Exception {
        accounts.findById(accFromMx)
            .ifPresent(account -> {
                account.setBalance(Money.of(new BigDecimal("1000.00"), "MXN"));
                accounts.save(account);
            });
        // Deja al from con 0 (transferimos todo primero)
        var drain = new TransferRequest(accFromMx, accToMx, new BigDecimal("1000.00"), "MXN");
        String content = objectMapper.writeValueAsString(drain);
        System.out.println("drain: " + content);
        ;
        mvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "drain-all")
                .content(content))
            .andExpect(status().isOk());

        var beforeFrom = accounts.findById(accFromMx).orElseThrow().getBalance().getAmount();
        var beforeTo = accounts.findById(accToMx).orElseThrow().getBalance().getAmount();

        // Intento con fondos insuficientes
        var body = new TransferRequest(accFromMx, accToMx, new BigDecimal("1.00"), "MXN");
        mvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "insufficient-1")
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.title").value("Insufficient funds"));

        var afterFrom = accounts.findById(accFromMx).orElseThrow().getBalance().getAmount();
        var afterTo = accounts.findById(accToMx).orElseThrow().getBalance().getAmount();

        assertThat(afterFrom).isEqualByComparingTo(beforeFrom);
        assertThat(afterTo).isEqualByComparingTo(beforeTo);
    }

    @Test
    void transfer_currency_mismatch_returns_400_and_no_balance_change() throws Exception {
        var beforeFrom = accounts.findById(accFromMx).orElseThrow().getBalance().getAmount();
        var beforeUsd = accounts.findById(accUsd).orElseThrow().getBalance().getAmount();

        // from MXN -> to USD con currency "MXN" debe fallar (mismatch)
        var body = new TransferRequest(accFromMx, accUsd, new BigDecimal("10.00"), "MXN");
        mvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "cur-mismatch")
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"));

        var afterFrom = accounts.findById(accFromMx).orElseThrow().getBalance().getAmount();
        var afterUsd = accounts.findById(accUsd).orElseThrow().getBalance().getAmount();

        assertThat(afterFrom).isEqualByComparingTo(beforeFrom);
        assertThat(afterUsd).isEqualByComparingTo(beforeUsd);
    }

    @Test
    void transfer_with_unknown_accounts_returns_404() throws Exception {
        var body = new TransferRequest(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("5.00"), "MXN");

        mvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "not-found-1")
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Not Found"));
    }
}
