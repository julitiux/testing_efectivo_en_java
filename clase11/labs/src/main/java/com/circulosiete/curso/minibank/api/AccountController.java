package com.circulosiete.curso.minibank.api;

import com.circulosiete.curso.minibank.commands.CreateAccount;
import com.circulosiete.curso.minibank.commands.CreditAccount;
import com.circulosiete.curso.minibank.commands.DebitAccount;
import com.circulosiete.curso.minibank.model.Account;
import com.circulosiete.curso.minibank.service.AccountCommandService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountCommandService service;

    static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    @PostMapping("/{id}/credit")
    public ResponseEntity<AccountView> credit(
        @PathVariable("id") UUID id,
        @RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey,
        @Valid @RequestBody AmountRequest body
    ) {
        final var cmd = new CreditAccount(
            id,
            body.getAmount(),
            body.getCurrency(),
            idempotencyKey
        );
        final var acc = service.handle(cmd);

        return ResponseEntity.ok(toView(acc));
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<AccountView> debit(
        @PathVariable("id") UUID id,
        @RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey,
        @Valid @RequestBody AmountRequest body
    ) {
        var cmd = new DebitAccount(id, body.getAmount(), body.getCurrency(), idempotencyKey);
        var acc = service.handle(cmd);
        return ResponseEntity.ok(toView(acc));
    }

    @PostMapping
    public ResponseEntity<AccountView> create(@Valid @RequestBody CreateAccount createAccount) {
        final var acc = this.service.createAccount(createAccount);
        return ResponseEntity.ok(toView(acc));
    }

    public static AccountView toView(Account a) {
        return new AccountView(
            a.getId(),
            a.getAccountNumber(),
            a.getType().name(),
            a.getStatus().name(),
            a.getBalance().getAmount(),
            a.getBalance().getCurrency(),
            a.getVersion()
        );
    }
}
