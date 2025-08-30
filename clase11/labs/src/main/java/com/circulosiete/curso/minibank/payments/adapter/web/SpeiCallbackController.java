package com.circulosiete.curso.minibank.payments.adapter.web;

import com.circulosiete.curso.minibank.payments.ports.out.PaymentRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/callbacks/spei")
@RequiredArgsConstructor
public class SpeiCallbackController {
    private final PaymentRepository payments;

    /**
     * Simulación de webhook de liquidación.
     */
    @PostMapping("/settlement")
    public ResponseEntity<?> settled(
        @RequestBody Map<String, String> payload
    ) {
        var externalRef = payload.getOrDefault("externalRef", "");
        return payments.findByExternalRef(externalRef)
            .map(p -> {
                p.markSettled();
                payments.save(p);
                return ResponseEntity.ok(Map.of("status", "ACK"));
            })
            .orElse(ResponseEntity.badRequest().body(Map.of("error", "unknown ref")));
    }
}
