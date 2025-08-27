package com.circulosiete.curso.testing.efectivo.clase10.lab02;

import java.math.BigDecimal;

public record OrderRequest(
    String orderId,
    String customerId,
    String customerEmail,
    String sku,
    int units,
    BigDecimal unitPrice
) {
    public BigDecimal total() {
        return unitPrice.multiply(BigDecimal.valueOf(units));
    }
}
