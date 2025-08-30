package com.circulosiete.curso.minibank.payments.adapter.web;


import com.circulosiete.curso.minibank.model.Money;
import com.circulosiete.curso.minibank.payments.domain.Payment;
import com.circulosiete.curso.minibank.payments.domain.PaymentState;
import com.circulosiete.curso.minibank.payments.ports.out.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpeiCallbackController.class)
class SpeiCallbackControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @MockitoBean
    PaymentRepository repo;

    private Payment newSentPayment() {
        var money = Money.of(new BigDecimal("100.00"), "MXN");
        var p = new Payment(
            UUID.randomUUID(),
            "req-1",
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            "002010077777777771",
            "002",
            money
        );
        p.setState(PaymentState.INITIATED);

        p.markSent("SPEI-req-1"); // deja el agregado en estado SENT
        return p;
    }

    @Test
    void should_ack_on_known_external_ref_and_mark_settled() throws Exception {
        var payment = newSentPayment();
        when(repo.findByExternalRef("SPEI-req-1")).thenReturn(Optional.of(payment));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/callbacks/spei/settlement")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("externalRef", "SPEI-req-1"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACK"));

        verify(repo).save(any(Payment.class));
    }

    @Test
    void should_return_400_when_unknown_external_ref() throws Exception {
        when(repo.findByExternalRef("NOPE")).thenReturn(Optional.empty());

        mvc.perform(post("/callbacks/spei/settlement")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("externalRef", "NOPE"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("unknown ref"));
    }
}

