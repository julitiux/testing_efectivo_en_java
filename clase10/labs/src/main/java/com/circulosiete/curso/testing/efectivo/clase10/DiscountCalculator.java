package com.circulosiete.curso.testing.efectivo.clase10;

import java.math.BigDecimal;

public class DiscountCalculator {
    // Descuento del 10% si total >= 100; nunca negativo
    public BigDecimal apply(BigDecimal total) {
        if (total == null) {
            throw new IllegalArgumentException("total");
        }
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (total.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return total.multiply(BigDecimal.valueOf(0.90));
        }
        return total;
    }
}
