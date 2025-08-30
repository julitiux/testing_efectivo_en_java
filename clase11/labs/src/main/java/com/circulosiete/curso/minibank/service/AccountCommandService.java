package com.circulosiete.curso.minibank.service;

import com.circulosiete.curso.minibank.commands.AccountCredited;
import com.circulosiete.curso.minibank.commands.AccountDebited;
import com.circulosiete.curso.minibank.commands.CreateAccount;
import com.circulosiete.curso.minibank.commands.CreditAccount;
import com.circulosiete.curso.minibank.commands.DebitAccount;
import com.circulosiete.curso.minibank.model.Account;
import com.circulosiete.curso.minibank.model.Money;
import com.circulosiete.curso.minibank.model.ProcessedOperation;
import com.circulosiete.curso.minibank.repository.AccountRepository;
import com.circulosiete.curso.minibank.repository.ProcessedOperationRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountCommandService {
    private final AccountRepository accounts;
    private final ProcessedOperationRepository processedOps;
    private final ApplicationEventPublisher events;

    @Transactional
    public Account createAccount(CreateAccount createAccount) {
        final var account = Account.open(
            createAccount.accountNumber(),
            createAccount.type(),
            UUID.randomUUID(),
            Money.zero(
                createAccount.currency()
            )
        );

        return this.accounts.save(account);
    }

    @Transactional
    public Account handle(CreditAccount cmd) {
        var account = accounts.findById(cmd.accountId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Idempotencia: inserta registro; si existe, no reprocesa
        ensureNotProcessed(account.getId(), cmd.requestId());

        account.credit(
            Money.of(
                cmd.amount(),
                cmd.currency()
            )
        );
        accounts.save(account);
        events.publishEvent(
            AccountCredited.now(
                account.getId(),
                cmd.amount(),
                cmd.currency()
            )
        );
        return account;
    }

    @Transactional
    public Account handle(DebitAccount cmd) {
        var account = accounts.findById(cmd.accountId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        ensureNotProcessed(account.getId(), cmd.requestId());

        account.debit(
            Money.of(
                cmd.amount(),
                cmd.currency()
            )
        );
        accounts.save(account);

        events.publishEvent(new AccountDebited(
            account.getId(), cmd.amount(), cmd.currency(), Instant.now()
        ));
        return account;
    }

    private void ensureNotProcessed(UUID accountId, String requestId) {
        try {
            processedOps.saveAndFlush(ProcessedOperation.now(accountId, requestId));
        } catch (DataIntegrityViolationException dup) {
            // Ya fue procesado exactamente el mismo requestId para esta cuenta
            throw new AlreadyProcessedException("Duplicate requestId for account");
        }
    }

    public static class AlreadyProcessedException extends RuntimeException {
        public AlreadyProcessedException(String msg) {
            super(msg);
        }
    }
}
