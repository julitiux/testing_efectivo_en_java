package com.circulosiete.curso.testing.clase03.steps;

import com.circulosiete.curso.testing.clase03.Account;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ServiceTransferSteps {
    private UUID origenAccountId;
    private UUID destinoAccountId;
    private Exception exception;

    @Given("una cuenta origen con saldo {double}")
    public void unaCuentaOrigenConSaldo(double balance) {
        this.origenAccountId = Hooks.transferContext().getAccountRepository()
                .save(new Account(
                        UUID.randomUUID(),
                        BigDecimal.valueOf(balance))
                )
                .getUuid();
    }

    @Given("una cuenta destino con saldo {double}")
    public void unaCuentaDestinoConSaldo(double balance) {
        this.destinoAccountId = Hooks.transferContext().getAccountRepository()
                .save(new Account(
                        UUID.randomUUID(),
                        BigDecimal.valueOf(balance))
                )
                .getUuid();
    }

    @When("transfiero {double} de origen a destino")
    public void transfieroDeOrigenADestino(double amount) {
        try {
            Hooks.transferContext().getTransferService().transfer(
                    this.origenAccountId,
                    this.destinoAccountId,
                    BigDecimal.valueOf(amount)
            );
        } catch (Exception exception) {
            this.exception = exception;
        }
    }

    @Then("el saldo de la cuenta origen debe ser {double}")
    public void elSaldoDeLaCuentaOrigenDebeSer(double expected) {
        Hooks.transferContext().getAccountRepository()
                .findById(this.origenAccountId)
                .ifPresentOrElse(
                        account -> assertThat(account.getBalance())
                                .isEqualByComparingTo(BigDecimal.valueOf(expected)),
                        () -> fail("El saldo de la cuenta origen no existe"));

    }

    @Then("el saldo de la cuenta destino debe ser {double}")
    public void elSaldoDeLaCuentaDestinoDebeSer(double expected) {
        Hooks.transferContext().getAccountRepository()
                .findById(this.destinoAccountId)
                .ifPresentOrElse(
                        account -> assertThat(account.getBalance())
                                .isEqualByComparingTo(BigDecimal.valueOf(expected)),
                        () -> fail("El saldo de la cuenta destino no existe"));
    }

    @Then("se debe lanzar una {word} con el mensaje {string}")
    public void se_debe_lanzar_una_exception_con_el_mensaje(String exceptionSimpleName, String string) {
        assertThat(exception).isNotNull();
        assertThat(exceptionSimpleName).isEqualTo(exception.getClass().getSimpleName());
        assertThat(exception.getMessage()).contains(string);
    }
}
