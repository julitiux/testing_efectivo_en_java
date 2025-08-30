package com.circulosiete.curso.minibank.api;

import com.circulosiete.curso.minibank.commands.TransferFunds;
import com.circulosiete.curso.minibank.repository.AccountRepository;
import com.circulosiete.curso.minibank.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.circulosiete.curso.minibank.api.AccountController.IDEMPOTENCY_KEY;


@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService service;
    private final AccountRepository accounts;

    @PostMapping
    public ResponseEntity<TransferView> transfer(@RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey,
                                                 @Valid @RequestBody TransferRequest body) {
        var transferId = service.handle(new TransferFunds(
            body.fromAccountId(), body.toAccountId(),
            body.amount(), body.currency(), idempotencyKey
        ));

        var from = accounts.findById(body.fromAccountId()).orElseThrow();
        var to   = accounts.findById(body.toAccountId()).orElseThrow();

        return ResponseEntity.ok(TransferView.builder()
            .transferId(transferId)
            .fromAccountId(from.getId())
            .toAccountId(to.getId())
            .amount(body.amount())
            .currency(body.currency())
            .from(AccountController.toView(from))
            .to(AccountController.toView(to))
            .build());
    }
}
