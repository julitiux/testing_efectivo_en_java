package com.circulosiete.curso.minibank.payments;


import com.circulosiete.curso.minibank.payments.domain.PaymentState;
import com.circulosiete.curso.minibank.payments.ports.in.GetPaymentStatus;
import com.circulosiete.curso.minibank.payments.ports.out.AccountLedgerPort;
import com.circulosiete.curso.minibank.payments.ports.out.AmlPort;
import com.circulosiete.curso.minibank.payments.ports.out.BankDirectoryPort;
import com.circulosiete.curso.minibank.payments.ports.out.PaymentRailPort;
import com.circulosiete.curso.minibank.payments.ports.out.PaymentRailRegistry;
import com.circulosiete.curso.minibank.payments.ports.out.PaymentRepository;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
class PaymentsFlowSpringBootTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;
    @Autowired
    PaymentRepository repo;

    @MockitoBean
    BankDirectoryPort directory;
    @MockitoBean
    AmlPort aml;
    @MockitoBean
    AccountLedgerPort ledger;
    @MockitoBean
    PaymentRailRegistry railRegistry;
    @MockitoBean
    PaymentRailPort rail;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void end_to_end_create_then_settle_via_webhook() {
        // Arrange: mocks de puertos externos
        when(directory.isValidAccount(any(), any(), any())).thenReturn(true);
        when(aml.passesAml(any(), any(), any(), any())).thenReturn(true);
        when(ledger.hold(any(), any(), any(), any(), any())).thenReturn("HOLD-1");
        when(railRegistry.resolve(any(), any())).thenReturn(rail);
        when(rail.send(any())).thenReturn(new PaymentRailPort.TransferResponse("SPEI-req-1", true, "OK"));

        // 1) POST /api/payments
        var body = Map.of(
            "fromAccountId", "00000000-0000-0000-0000-000000000001",
            "toAccount", "002010077777777771",
            "toBankId", "002",
            "currency", "MXN",
            "amount", 123.45,
            "purpose", "demo",
            "paymentReference", "sss"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", "req-1");

        ResponseEntity<Map> createResp = rest.exchange(
            baseUrl() + "/api/payments",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            Map.class
        );

        if(createResp.getStatusCode().isError()) {
            System.out.println(createResp.getBody());
        }

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        var paymentId = UUID.fromString(createResp.getBody().get("paymentId").toString());

        // Verifica estado en DB (SENT)
        var p1 = repo.findById(paymentId).orElseThrow();
        assertThat(p1.getState()).isEqualTo(PaymentState.SENT);
        assertThat(p1.getExternalRef()).isEqualTo("SPEI-req-1");

        // 2) POST webhook /callbacks/spei/settlement
        ResponseEntity<Map> cb = rest.postForEntity(
            baseUrl() + "/callbacks/spei/settlement",
            Map.of("externalRef", "SPEI-req-1"),
            Map.class
        );
        assertThat(cb.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cb.getBody().get("status")).isEqualTo("ACK");

        // Verifica estado en DB (SETTLED)
        var p2 = repo.findById(paymentId).orElseThrow();
        assertThat(p2.getState()).isEqualTo(PaymentState.SETTLED);

        // 3) Idempotencia: mismo requestId → mismo paymentId
        ResponseEntity<Map> createAgain = rest.exchange(
            baseUrl() + "/api/payments",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            Map.class
        );
        assertThat(createAgain.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        var paymentId2 = UUID.fromString(createAgain.getBody().get("paymentId").toString());
        assertThat(paymentId2).isEqualTo(paymentId);
    }

    /**
     * Bean de apoyo para poder resolver GET /api/payments/{id} si lo usas en tus pruebas.
     * Implementa GetPaymentStatus leyendo del repositorio.
     */
    @TestConfiguration
    static class TestBeans {
        @Bean
        @Primary
        GetPaymentStatus getPaymentStatus(PaymentRepository repo) {
            return q -> repo.findById(q.paymentId())
                .map(p -> new GetPaymentStatus.Result(
                    p.getId(),
                    p.getState().name(),
                    GetPaymentStatus.ExternalStatus.PENDING,
                    p.getUpdatedAt()
                ))
                .orElseThrow();
        }
    }
}

