package com.circulosiete.curso.minibank.commands;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @param requestId idempotencia
 */
public record CreditAccount(
    UUID accountId,
    BigDecimal amount,
    String currency,
    String requestId) {
}
