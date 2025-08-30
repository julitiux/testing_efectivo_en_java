package com.circulosiete.curso.minibank.payments.adapter.spei;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.circulosiete.curso.minibank.payments.ports.out.PaymentRailPort;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "spei-gateway", pactVersion = PactSpecVersion.V3)
class SpeiPaymentRailHttpAdapterPactTest {

    @Pact(consumer = "minibank-payments")
    RequestResponsePact send_credit_transfer(PactDslWithProvider builder) {
        return builder
            .uponReceiving("POST credit transfer accepted")
            .path("/payments/credit-transfer")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body("""
                {
                  "requestId":"req-1",
                  "debtorAccount":"00000000-0000-0000-0000-000000000001",
                  "creditorAccount":"002010077777777771",
                  "creditorBankId":"002",
                  "currency":"MXN",
                  "amount":"123.45",
                  "purpose":"demo"
                }
                """)
            .willRespondWith()
            .status(202)
            .headers(Map.of("Content-Type", "application/json"))
            .body("""
                {
                  "externalRef":"SPEI-req-1",
                  "accepted":true,
                  "message":"OK"
                }
                """)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "send_credit_transfer")
    void test_send_should_return_externalRef_and_accepted(MockServer server) {
        var adapter = new SpeiPaymentRailHttpAdapter(server.getUrl(), new RestTemplate());
        var resp = adapter.send(new PaymentRailPort.TransferRequest(
            "req-1",
            "00000000-0000-0000-0000-000000000001",
            "002010077777777771",
            "002",
            "MXN",
            "123.45",
            "demo"
        ));
        assertThat(resp.accepted()).isTrue();
        assertThat(resp.externalRef()).isEqualTo("SPEI-req-1");
    }
}

