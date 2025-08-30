package com.circulosiete.curso.minibank.payments.adapter.web;



import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.circulosiete.curso.minibank.payments.ports.in.GetPaymentStatus;
import com.circulosiete.curso.minibank.payments.ports.in.InitiateInterbankTransfer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentsController.class)
class PaymentsControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean
    InitiateInterbankTransfer initiate;
    @MockitoBean
    GetPaymentStatus status;

    @Test
    void should_create_payment_and_return_202_with_id() throws Exception {
        var paymentId = UUID.randomUUID();
        when(initiate.handle(any())).thenReturn(paymentId);

        Map<String, Object> body = new HashMap<>();
        body.put("fromAccountId", "00000000-0000-0000-0000-000000000001");
        body.put("toAccount", "002010077777777771");
        body.put("toBankId", "002");
        body.put("currency", "MXN");
        body.put("amount", 123.45);
        body.put("purpose", "demo");

        mvc.perform(post("/api/payments")
                .header("Idempotency-Key", "req-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isAccepted())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.paymentId").value(paymentId.toString()));
    }

    @Test
    void should_return_400_when_missing_idempotency_header() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("fromAccountId", "00000000-0000-0000-0000-000000000001");
        body.put("toAccount", "002010077777777771");
        body.put("toBankId", "002");
        body.put("currency", "MXN");
        body.put("amount", 10);

        mvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void should_get_payment_status() throws Exception {
        var id = UUID.randomUUID();
        var result = new GetPaymentStatus.Result(id, "SENT",
            GetPaymentStatus.ExternalStatus.PENDING, Instant.parse("2025-01-01T00:00:00Z"));

        when(status.handle(any())).thenReturn(result);

        mvc.perform(get("/api/payments/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value(id.toString()))
            .andExpect(jsonPath("$.state").value("SENT"))
            .andExpect(jsonPath("$.externalStatus").value("PENDING"))
            .andExpect(jsonPath("$.updatedAt").value("2025-01-01T00:00:00Z"));
    }
}

