package com.circulosiete.curso.minibank.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
    @NotNull UUID fromAccountId,
    @NotNull UUID toAccountId,
    @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal amount,
    @NotBlank @Size(min = 3, max = 3) String currency
) {
}
