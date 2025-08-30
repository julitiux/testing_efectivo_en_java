package com.circulosiete.curso.minibank.commands;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountCredited(
    UUID accountId,
    BigDecimal amount,
    String currency,
    Instant occurredAt
) {

    public static AccountCredited now(
        UUID accountId,
        BigDecimal amount,
        String currency
    ) {
        return new AccountCredited(
            accountId,
            amount,
            currency,
            Instant.now()
        );
    }
}
