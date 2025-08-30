package com.circulosiete.curso.minibank.api;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountView(
    UUID id,
    String accountNumber,
    String type,
    String status,
    BigDecimal balance,
    String currency,
    Long version
) {
}
