Feature: Cálculo de envío

  Scenario: Cliente obtiene envío gratis
    Given que el cliente tiene un carrito con valor de 700 pesos y el costo minimo para envio gratis es 500
    When el cliente procede al pago
    Then el costo de envío debe ser 0
