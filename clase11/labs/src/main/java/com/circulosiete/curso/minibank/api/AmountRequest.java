package com.circulosiete.curso.minibank.api;

import jakarta.validation.constraints.*;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class AmountRequest {
    @NotNull @DecimalMin(value = "0.00", inclusive = false)
    BigDecimal amount;

    @NotBlank @Size(min = 3, max = 3)
    String currency; // ISO-4217
}
