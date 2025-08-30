package com.circulosiete.curso.minibank.payments.domain;

import com.circulosiete.curso.minibank.model.Money;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class Payment {

    private final UUID id;
    private final String requestId;         // Idempotencia externa
    private final UUID fromAccountId;
    private final String toBankAccount;     // CLABE/IBAN
    private final String toBankId;          // BIC/Institution Id
    private final Money amount;
    private PaymentState state;
    private String externalRef;             // id en el riel
    private Instant createdAt;
    private Instant updatedAt;

    // invariantes y transición de estados
    public void markSent(String externalRef) {
        this.ensure(PaymentState.INITIATED);
        this.state = PaymentState.SENT;
        this.externalRef = externalRef;
        touch();
    }

    public void markSettled() {
        ensure(PaymentState.SENT);
        this.state = PaymentState.SETTLED;
        touch();
    }

    public void markFailed(String reason) {
        this.state = PaymentState.FAILED;
        touch(); /* registrar reason en event store */
    }

    public void cancel() {
        if (state == PaymentState.SETTLED) {
            throw new IllegalStateException("Already settled");
        }
        this.state = PaymentState.CANCELLED;
        touch();
    }

    private void ensure(PaymentState expected) {
        if (this.state != expected) {
            throw new IllegalStateException("Expected " + expected + " but was " + this.state);
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
