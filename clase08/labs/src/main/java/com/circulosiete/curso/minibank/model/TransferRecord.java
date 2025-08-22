package com.circulosiete.curso.minibank.model;

import com.circulosiete.curso.minibank.commands.TransferFunds;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "transfer",
    uniqueConstraints = @UniqueConstraint(
        name = "transfer_request",
        columnNames = "request_id")
)
public class TransferRecord {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @Column(name = "from_account_id", nullable = false)
    private UUID fromAccountId;

    @Column(name = "to_account_id", nullable = false)
    private UUID toAccountId;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public enum Status {PENDING, COMPLETED}

    public static TransferRecord pending(TransferFunds cmd) {
        return TransferRecord.builder()
            .requestId(cmd.requestId())
            .fromAccountId(cmd.fromAccountId())
            .toAccountId(cmd.toAccountId())
            .amount(cmd.amount())
            .currency(cmd.currency())
            .status(Status.PENDING)
            .createdAt(Instant.now())
            .build();
    }

    public void complete() {
        this.status = Status.COMPLETED;
        this.completedAt = Instant.now();
    }

}
