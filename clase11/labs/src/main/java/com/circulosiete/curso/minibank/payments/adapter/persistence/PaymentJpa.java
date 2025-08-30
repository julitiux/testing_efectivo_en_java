package com.circulosiete.curso.minibank.payments.adapter.persistence;

import com.circulosiete.curso.minibank.payments.domain.PaymentState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(
    name = "payment",
    indexes = @Index(
        name = "request_id",
        columnList = "request_id",
        unique = true
    )
)
@Getter
@Setter
@NoArgsConstructor
@ToString
public class PaymentJpa {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "request_id", nullable = false, length = 64, unique = true)
    private String requestId;

    @Column(name = "from_account_id", nullable = false)
    private UUID fromAccountId;

    @Column(name = "to_bank_account", nullable = false, length = 32)
    private String toBankAccount; // CLABE SPEI

    @Column(name = "to_bank_id", nullable = false, length = 16)
    private String toBankId; // Clave de institución (3 dígitos) o BIC si quisieras

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 16)
    private PaymentState state = PaymentState.INITIATED;

    @Column(name = "external_ref", length = 64)
    private String externalRef;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public void markSent(String externalRef) {
        ensure(PaymentState.INITIATED);
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
        touch(); /* log reason en outbox */
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
            throw new IllegalStateException("Expected " + expected + " but was " + state);
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
