package com.circulosiete.curso.minibank.bdd;

import com.circulosiete.curso.minibank.model.Account;
import com.circulosiete.curso.minibank.model.AccountType;
import com.circulosiete.curso.minibank.model.Money;
import com.circulosiete.curso.minibank.repository.AccountRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


public class TransferStepDefs {
    private final TransferSharedData shared;
    private final AccountRepository accounts;

    public TransferStepDefs(AccountRepository accounts) {
        this.accounts = accounts;
        this.shared = new TransferSharedData();
    }

    @Given("el sistema está sano")
    public void the_system_is_healthy() {
        // No-op; si tuviéramos /actuator/health aquí podríamos checarlo.
    }

    @Given("Existen las siguientes cuentas:")
    public void the_following_accounts_exist(DataTable table) {
        // Columns: name | balance | currency
        for (var row : table.asMaps()) {
            var name = row.get("name");
            var bal = new BigDecimal(row.get("balance"));
            var cur = row.get("currency");

            String accountNumber = "ACC-" + name;
            accounts.findByAccountNumber(accountNumber)
                .ifPresentOrElse(
                    account -> {
                        account.setBalance(Money.of(bal, cur));
                        var acc = accounts.save(account);
                        shared.idsByName.put(name, acc.getId());
                    },
                    () -> {
                        var acc = Account.open(
                            accountNumber,
                            AccountType.CURRENT,
                            UUID.randomUUID(),
                            Money.of(bal, cur)
                        );
                        accounts.save(acc);
                        shared.idsByName.put(name, acc.getId());
                    });

        }
    }

    @When("Transfiero {double} {word} de la cuenta {string} a la cuenta {string} con clave de idempotencia {string}")
    public void i_transfer_amount(double amount, String currency, String fromName, String toName, String idemKey) {
        var fromId = shared.idsByName.get(fromName);
        var toId = shared.idsByName.get(toName);

        var body = Map.of(
            "fromAccountId", fromId.toString(),
            "toAccountId", toId.toString(),
            "amount", new BigDecimal(amount),
            "currency", currency
        );

        shared.lastBody = new HashMap<>(body);
        shared.lastIdempotencyKey = idemKey;

        shared.lastResponse = given()
            .contentType(ContentType.JSON)
            .header("Idempotency-Key", idemKey)
            .body(body)
            .when()
            .post("/transfers")
            .then()
            .extract().response();
    }

    @When("Repito la misma transferencia")
    public void i_repeat_the_same_transfer() {
        shared.secondResponse = given()
            .contentType(ContentType.JSON)
            .header("Idempotency-Key", shared.lastIdempotencyKey)
            .body(shared.lastBody)
            .when()
            .post("/transfers")
            .then()
            .extract()
            .response();
    }

    @When("Envío una transferencia entre cuentas inexistentes con clave de idempotencia {string}")
    public void i_submit_transfer_non_existing(String idemKey) {
        var body = Map.of(
            "fromAccountId", UUID.randomUUID().toString(),
            "toAccountId", UUID.randomUUID().toString(),
            "amount", new BigDecimal("10.00"),
            "currency", "MXN"
        );
        shared.lastBody = new HashMap<>(body);
        shared.lastIdempotencyKey = idemKey;

        shared.lastResponse = given()
            .contentType(ContentType.JSON)
            .header("Idempotency-Key", idemKey)
            .body(body)
            .when()
            .post("/transfers")
            .then()
            .extract().response();
    }

    @Then("El código de respuesta debe ser {int}")
    public void the_response_status_should_be(Integer code) {
        shared.lastResponse.then().statusCode(code);
    }

    @Then("El segundo código de respuesta debe ser {int}")
    public void the_second_response_status_should_be(Integer code) {
        shared.secondResponse.then().statusCode(code);
    }

    @Then("El balance {string} debe ser {double} {string}")
    public void the_snapshot_balance_should_be(String which, double amount, String currency) {
        // which: "from" or "to"
        var f = BigDecimal.valueOf(amount);
        shared.lastResponse.then()
            .body(which + ".balance", equalTo((float) amount))
            .and()
            .body(which + ".currency", equalTo(currency));
    }

    @Then("Los saldos guardados deben ser:")
    public void the_persisted_balances_should_be(DataTable table) {
        for (var row : table.asMaps()) {
            var name = row.get("name");
            var expected = new BigDecimal(row.get("balance"));
            var id = shared.idsByName.get(name);
            var acc = accounts.findById(id).orElseThrow();

            Assertions.assertThat(acc.getBalance().getAmount()).isEqualByComparingTo(expected);
        }
    }
}
