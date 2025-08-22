package com.circulosiete.curso.minibank.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "account",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = "account_number"
        )
    },
    indexes = {
        @Index(
            name = "idx_account_customer",
            columnList = "customer_id"
        ),
        @Index(
            name = "idx_account_status",
            columnList = "status"
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
public class Account {
    @Id
    @GeneratedValue // Hibernate mapea UUID nativamente; en Postgres usa tipo uuid
    @Column(nullable = false, updatable = false)
    private UUID id;
    @Column(name = "account_number", nullable = false, length = 34)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
            name = "amount",
            column = @Column(
                name = "amount",
                precision = 19,
                scale = 4,
                nullable = false
            )
        ),
        @AttributeOverride(
            name = "currency",
            column = @Column(
                name = "currency",
                length = 3,
                nullable = false
            )
        )
    })
    private Money balance;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "overdraft_limit", precision = 19, scale = 4)
    private BigDecimal overdraftLimit;

    @Version
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // --- Reglas del agregado --
    public void credit(Money amount) {
        requireSameCurrency(amount);
        this.balance = this.balance.add(amount);
    }

    public void debit(Money amount) {
        requireSameCurrency(amount);
        var newBalance = this.balance.subtract(amount);
        if (!canGoNegative(newBalance)) {
            throw new IllegalStateException("Insufficient funds");
        }
        this.balance = newBalance;
    }

    private boolean canGoNegative(Money newBalance) {
        if (overdraftLimit == null) {
            return newBalance.getAmount().signum() >= 0;
        }
        var limit = overdraftLimit.negate(); // p.ej. -1000.00
        return newBalance.getAmount().compareTo(limit) >= 0;
    }

    private void requireSameCurrency(Money amount) {
        if (!this.balance.getCurrency().equals(amount.getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch");
        }
    }

    public static Account open(
        String number,
        AccountType type,
        UUID customerId,
        Money opening
    ) {
        return Account.builder()
            .accountNumber(number)
            .type(type)
            .status(AccountStatus.ACTIVE)
            .customerId(customerId)
            .balance(opening != null ? opening : Money.zero("MXN"))
            .build();
    }
}
