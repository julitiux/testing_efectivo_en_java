package com.circulosiete.curso.minibank.service;

import com.circulosiete.curso.minibank.commands.FundsTransferred;
import com.circulosiete.curso.minibank.commands.TransferFunds;
import com.circulosiete.curso.minibank.model.Money;
import com.circulosiete.curso.minibank.model.TransferRecord;
import com.circulosiete.curso.minibank.repository.AccountRepository;
import com.circulosiete.curso.minibank.repository.TransferRecordRepository;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final AccountRepository accounts;
    private final TransferRecordRepository transfers;
    private final ApplicationEventPublisher events;

    @Transactional
    public UUID handle(TransferFunds cmd) {
        if (cmd.fromAccountId().equals(cmd.toAccountId())) {
            throw new IllegalArgumentException("From and To accounts must differ");
        }
        if (cmd.amount() == null || cmd.amount().signum() <= 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }

        // Idempotencia (fail-fast): requestId único
        try {
            transfers.saveAndFlush(TransferRecord.pending(cmd));
        } catch (DataIntegrityViolationException dup) {
            throw new AlreadyProcessedException("Duplicate requestId for transfer");
        }

        // Bloqueo en orden estable para evitar deadlocks
        var orderedIds = Stream.of(
                cmd.fromAccountId(),
                cmd.toAccountId())
            .sorted(Comparator.naturalOrder())
            .toList();

        var first = accounts.lockByIdForUpdate(orderedIds.get(0))
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + orderedIds.getFirst()));
        var second = accounts.lockByIdForUpdate(orderedIds.get(1))
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + orderedIds.get(1)));

        var from = first.getId().equals(cmd.fromAccountId()) ? first : second;
        var to = from == first ? second : first;

        // Moneda consistente (simple). Si quieres FX, aquí iría el cálculo.
        var cur = cmd.currency();
        if (!from.getBalance().getCurrency().equals(cur) || !to.getBalance().getCurrency().equals(cur)) {
            throw new IllegalArgumentException("Currency mismatch");
        }

        // Reglas del agregado
        from.debit(Money.of(cmd.amount(), cur)); // valida fondos/overdraft
        to.credit(Money.of(cmd.amount(), cur));

        accounts.save(from);
        accounts.save(to);

        var rec = transfers.findByRequestId(cmd.requestId()).orElseThrow();
        rec.complete();
        transfers.save(rec);

        events.publishEvent(
            new FundsTransferred(
                rec.getId(), from.getId(), to.getId(), cmd.amount(), cur, java.time.Instant.now()
            ));
        return rec.getId();
    }

    public static class AlreadyProcessedException extends RuntimeException {
        public AlreadyProcessedException(String msg) {
            super(msg);
        }
    }
}
