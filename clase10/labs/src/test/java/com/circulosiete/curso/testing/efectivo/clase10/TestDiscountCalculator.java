package com.circulosiete.curso.testing.efectivo.clase10;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestDiscountCalculator {
    @Test
    void appliesTenPercentForTotalsOver100() {
        var sut = new DiscountCalculator();
        assertEquals(new BigDecimal("90.0"), sut.apply(new BigDecimal("100")));
    }

    @Test
    void handlesBoundariesAndNegatives() {
        var sut = new DiscountCalculator();
        // Borde exacto
        assertEquals(new BigDecimal("90.0"), sut.apply(new BigDecimal("100")));
        // Justo debajo del borde
        assertEquals(new BigDecimal("99.99"), sut.apply(new BigDecimal("99.99")));
        // Negativo -> 0
        assertEquals(BigDecimal.ZERO, sut.apply(new BigDecimal("-1")));
        // Nulo -> excepción
        assertThrows(IllegalArgumentException.class, () -> sut.apply(null));
    }
}
