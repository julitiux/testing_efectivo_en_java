package com.circulosiete.curso.testing.efectivo.clase10;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CalculatorTest {

    private final Calculator calculator = new Calculator();

    @Test
    void testAdd() {
        assertEquals(5, calculator.add(2, 3));
    }

    @Test
    void testIsPositive() {
        assertTrue(calculator.isPositive(10));
        assertFalse(calculator.isPositive(-5));
        assertFalse(calculator.isPositive(0)); // este test es útil para matar mutaciones
    }

    @Test
    void testMax() {
        assertEquals(5, calculator.max(5, 3));
        assertEquals(7, calculator.max(4, 7));
    }
}
