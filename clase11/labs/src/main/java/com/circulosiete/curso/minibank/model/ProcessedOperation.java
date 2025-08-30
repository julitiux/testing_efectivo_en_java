package com.circulosiete.curso.minibank.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "processed_operation",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {
            "account_id",
            "request_id"
        }
    )
)
public class ProcessedOperation {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static ProcessedOperation now(UUID accountId, String requestId) {
        return ProcessedOperation.builder()
            .accountId(accountId)
            .requestId(requestId)
            .createdAt(Instant.now())
            .build();
    }
}
