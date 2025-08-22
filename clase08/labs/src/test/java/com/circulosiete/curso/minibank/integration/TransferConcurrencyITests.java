package com.circulosiete.curso.minibank.integration;

import com.circulosiete.curso.minibank.commands.TransferFunds;
import com.circulosiete.curso.minibank.model.Account;
import com.circulosiete.curso.minibank.model.AccountType;
import com.circulosiete.curso.minibank.model.Money;
import com.circulosiete.curso.minibank.repository.AccountRepository;
import com.circulosiete.curso.minibank.service.TransferService;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class TransferConcurrencyITests {
    @Container
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    TransferService transferService;
    @Autowired
    AccountRepository accounts;

    UUID accA;
    UUID accB;

    @BeforeEach
    void setUp() {
        // A: 1000 MXN, B: 100 MXN
        accounts.findByAccountNumber("ACC-A")
            .ifPresentOrElse(
                account -> {
                    account.setBalance(Money.of(new BigDecimal("1000.00"), "MXN"));
                    accounts.save(account);
                    accA = account.getId();
                },
                () -> {
                    var acc = Account.open("ACC-A", AccountType.CURRENT, UUID.randomUUID(),
                        Money.of(new BigDecimal("1000.00"), "MXN"));
                    accA = accounts.save(acc).getId();
                }
            );
        accounts.findByAccountNumber("ACC-B")
            .ifPresentOrElse(
                account -> {
                    account.setBalance(Money.of(new BigDecimal("100.00"), "MXN"));
                    accounts.save(account);
                    accB = account.getId();
                },
                () -> {
                    var acc = Account.open("ACC-B", AccountType.CURRENT, UUID.randomUUID(),
                        Money.of(new BigDecimal("100.00"), "MXN"));
                    accB = accounts.save(acc).getId();
                }
            );
    }

    @Test
    void concurrent_transfers_from_same_source_are_serialized_correctly() throws Exception {
        // Dos transferencias en paralelo desde A -> B, 400 cada una
        var t1 = new TransferFunds(accA, accB, new BigDecimal("400.00"), "MXN", "con-same-1");
        var t2 = new TransferFunds(accA, accB, new BigDecimal("400.00"), "MXN", "con-same-2");

        runInParallel(() -> transferService.handle(t1),
            () -> transferService.handle(t2));

        var a = accounts.findById(accA).orElseThrow(); // 1000 - 400 - 400 = 200
        var b = accounts.findById(accB).orElseThrow(); // 100  + 400 + 400 = 900
        assertThat(a.getBalance().getAmount()).isEqualByComparingTo("200.00");
        assertThat(b.getBalance().getAmount()).isEqualByComparingTo("900.00");
    }

    @Test
    void opposing_concurrent_transfers_do_not_deadlock_and_apply_both() throws Exception {
        // Reinicializa saldos: A 500, B 500
        var a = accounts.findById(accA).orElseThrow();
        var b = accounts.findById(accB).orElseThrow();
        a.setBalance(Money.of(new BigDecimal("500.00"), "MXN"));
        b.setBalance(Money.of(new BigDecimal("500.00"), "MXN"));
        accounts.save(a);
        accounts.save(b);

        // T1: A -> B (100), T2: B -> A (200) en paralelo
        var t1 = new TransferFunds(accA, accB, new BigDecimal("100.00"), "MXN", "con-x-1");
        var t2 = new TransferFunds(accB, accA, new BigDecimal("200.00"), "MXN", "con-x-2");

        runInParallel(() -> transferService.handle(t1),
            () -> transferService.handle(t2));

        a = accounts.findById(accA).orElseThrow(); // 500 -100 +200 = 600
        b = accounts.findById(accB).orElseThrow(); // 500 +100 -200 = 400
        assertThat(a.getBalance().getAmount()).isEqualByComparingTo("600.00");
        assertThat(b.getBalance().getAmount()).isEqualByComparingTo("400.00");
    }

    @Test
    void two_concurrent_debits_one_fails_due_to_insufficient_funds() throws Exception {
        // A = 1000, B = 100 (desde @BeforeEach)
        var t1 = new TransferFunds(accA, accB, new BigDecimal("700.00"), "MXN", "race-700-1");
        var t2 = new TransferFunds(accA, accB, new BigDecimal("700.00"), "MXN", "race-700-2");

        var start = new CountDownLatch(1);
        var pool  = Executors.newFixedThreadPool(2);

        Future<?> f1 = pool.submit(() -> {
            await(start);
            transferService.handle(t1);
            return null;
        });
        Future<?> f2 = pool.submit(() -> {
            await(start);
            transferService.handle(t2);
            return null;
        });

        start.countDown(); // Disparar ambas a la vez

        int successCount = 0;
        int failureCount = 0;

        try {
            f1.get(10, TimeUnit.SECONDS);
            successCount++;
        } catch (ExecutionException ee) {
            assertThat(ee.getCause()).isInstanceOf(IllegalStateException.class);
            failureCount++;
        }
        try {
            f2.get(10, TimeUnit.SECONDS);
            successCount++;
        } catch (ExecutionException ee) {
            assertThat(ee.getCause()).isInstanceOf(IllegalStateException.class);
            failureCount++;
        } finally {
            pool.shutdownNow();
        }

        // Debe haber exactamente 1 éxito y 1 fallo
        assertThat(successCount).isEqualTo(1);
        assertThat(failureCount).isEqualTo(1);

        // Verifica saldos finales: sólo se aplicó una transferencia de 700
        var a = accounts.findById(accA).orElseThrow(); // 1000 - 700 = 300
        var b = accounts.findById(accB).orElseThrow(); // 100  + 700 = 800
        assertThat(a.getBalance().getAmount()).isEqualByComparingTo("300.00");
        assertThat(b.getBalance().getAmount()).isEqualByComparingTo("800.00");
    }

    // Helper pequeño para await sin ruido
    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }


    // --- Helper para disparar dos tareas a la vez y esperar resultado ---
    private void runInParallel(Callable<?> c1, Callable<?> c2) throws Exception {
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> f1 = pool.submit(() -> callWhen(start, c1));
            Future<?> f2 = pool.submit(() -> callWhen(start, c2));
            start.countDown(); // liberar ambas a la vez
            // timeouts conservadores para detectar deadlocks
            f1.get(10, TimeUnit.SECONDS);
            f2.get(10, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }

    private Object callWhen(CountDownLatch start, Callable<?> c) {
        try {
            start.await();
            return c.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
