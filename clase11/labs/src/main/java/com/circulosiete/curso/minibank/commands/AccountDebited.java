package com.circulosiete.curso.minibank.commands;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountDebited(
    UUID accountId,
    BigDecimal amount,
    String currency,
    Instant occurredAt) {
}
