package com.circulosiete.curso.minibank.payments.adapter.web;

import com.circulosiete.curso.minibank.payments.ports.in.GetPaymentStatus;
import com.circulosiete.curso.minibank.payments.ports.in.InitiateInterbankTransfer;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentsController {
    public record CreatePaymentDto(
        @NotBlank String fromAccountId,
        @NotBlank String toAccount,
        @NotBlank String toBankId,
        @NotBlank String currency,
        BigDecimal amount,
        String purpose,
        String paymentReference
    ) {
        public InitiateInterbankTransfer.Command toCommand(String requestId) {
            UUID idFromAccountId = UUID.fromString(fromAccountId);
            return new InitiateInterbankTransfer.Command(
                requestId,
                idFromAccountId,
                toAccount,
                toBankId,
                amount,
                currency,
                purpose,
                paymentReference
            );
        }
    }

    private final InitiateInterbankTransfer initiateInterbankTransfer;
    private final GetPaymentStatus status;

    @PostMapping
    public ResponseEntity<?> create(
        @RequestHeader("Idempotency-Key") String idemKey,
        @RequestBody CreatePaymentDto dto
    ) {
        final var command = dto.toCommand(idemKey);
        var id = initiateInterbankTransfer.handle(command);
        return ResponseEntity.accepted()
            .body(
                Map.of("paymentId", id)
            );
    }

    @GetMapping("/{id}")
    public GetPaymentStatus.Result get(@PathVariable UUID id) {
        return status.handle(new GetPaymentStatus.Query(id));
    }
}
