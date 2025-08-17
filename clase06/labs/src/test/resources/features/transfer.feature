Feature: Transferencia entre cuentas
  Como usuario del banco
  Quiero transferir dinero entre cuentas
  Para mover fondos de forma segura

  Scenario: Transferencia exitosa con saldo suficiente
    Given una cuenta origen con saldo 100.00
    And una cuenta destino con saldo 50.00
    When transfiero 30.00 de origen a destino
    Then el saldo de la cuenta origen debe ser 70.00
    And el saldo de la cuenta destino debe ser 80.00

  Scenario: Transferencia no exitosa por saldo suficiente
    Given una cuenta origen con saldo 10.00
    And una cuenta destino con saldo 0.00
    When transfiero 10.01 de origen a destino
    Then se debe lanzar una IllegalStateException con el mensaje "Insufficient funds"
    And el saldo de la cuenta origen debe ser 10.00
    And el saldo de la cuenta destino debe ser 0.00
