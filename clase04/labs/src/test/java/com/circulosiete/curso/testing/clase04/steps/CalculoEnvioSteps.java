package com.circulosiete.curso.testing.clase04.steps;

import com.circulosiete.curso.testing.clase04.ShippingCostCalculator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;


public class CalculoEnvioSteps {

  private BigDecimal totalCarrito;
  private BigDecimal costoMinimo;
  private ShippingCostCalculator shippingCostCalculator;

  @Given("que el cliente tiene un carrito con valor de {int} pesos y el costo minimo para envio gratis es {int}")
  public void given(Integer totalCarrito, Integer costoMinimo) {
    this.totalCarrito = BigDecimal.valueOf(totalCarrito);
    this.costoMinimo = BigDecimal.valueOf(costoMinimo);

    this.shippingCostCalculator = new ShippingCostCalculator(
      this.costoMinimo,
      BigDecimal.valueOf(50)
    );

  }

  @When("el cliente procede al pago")
  public void when() {

    var costoEnvio = this.shippingCostCalculator.calculate(
      this.totalCarrito
    );
  }

  @Then("el costo de envío debe ser {int}")
  public void then(Integer costoEnvio) {
    
  }

}
