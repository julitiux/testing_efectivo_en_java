package com.circulosiete.curso.minibank.payments.adapter.web;


import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import com.circulosiete.curso.minibank.model.Money;
import com.circulosiete.curso.minibank.payments.domain.Payment;
import com.circulosiete.curso.minibank.payments.domain.PaymentState;
import com.circulosiete.curso.minibank.payments.ports.out.PaymentRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("minibank-callbacks")
@PactFolder("src/test/resources/pacts")
@ActiveProfiles("test")
@Testcontainers
class SpeiCallbackControllerPactProviderTest {

    @LocalServerPort
    int port;
    @Autowired
    PaymentRepository repo;
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port, "/"));
    }

    /**
     * Prepara el estado del proveedor declarado en el pact file.
     */
    @State("payment exists SENT with externalRef SPEI-req-1")
    void paymentExistsSent() {
        var money = Money.of(new BigDecimal("123.45"), "MXN");
        var p = new Payment(
            null,
            "req-1",
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            "002010077777777771",
            "002",
            money
        );
        p.setState(PaymentState.INITIATED);
        p.markSent("SPEI-req-1");
        repo.save(p);

        assertThat(repo.findByExternalRef("SPEI-req-1")).isPresent();
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerification(PactVerificationContext context) {
        context.verifyInteraction();
    }
}

