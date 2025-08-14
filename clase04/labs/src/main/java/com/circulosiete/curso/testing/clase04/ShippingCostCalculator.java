package com.circulosiete.curso.testing.clase04;

import java.math.BigDecimal;

public class ShippingCostCalculator {

  private final BigDecimal valorMinimo;
  private final BigDecimal costoEnvio;

  public ShippingCostCalculator(BigDecimal valorMinimo, BigDecimal costoEnvio) {
    this.valorMinimo = valorMinimo;
    this.costoEnvio = costoEnvio;
  }

  public BigDecimal calculate(BigDecimal totalCarrito) {
    return totalCarrito.compareTo(this.valorMinimo) >= 0
      ? BigDecimal.ZERO : this.costoEnvio;
  }
}
