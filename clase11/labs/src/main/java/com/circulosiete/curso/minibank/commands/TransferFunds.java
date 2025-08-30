package com.circulosiete.curso.minibank.commands;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @param currency  ISO-4217
 * @param requestId Idempotency-Key
 */
public record TransferFunds(
    UUID fromAccountId,
    UUID toAccountId,
    BigDecimal amount,
    String currency,
    String requestId) {
}
