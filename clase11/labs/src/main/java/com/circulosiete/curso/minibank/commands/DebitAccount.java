package com.circulosiete.curso.minibank.commands;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @param requestId idempotencia
 */
public record DebitAccount(
    UUID accountId,
    BigDecimal amount,
    String currency,
    String requestId) {
}
