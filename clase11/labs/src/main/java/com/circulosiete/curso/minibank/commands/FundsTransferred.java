package com.circulosiete.curso.minibank.commands;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FundsTransferred(
    UUID transferId,
    UUID fromAccountId,
    UUID toAccountId,
    BigDecimal amount,
    String currency,
    Instant occurredAt
) {
}
