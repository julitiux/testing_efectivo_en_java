package com.circulosiete.curso.testing.clase03;

import com.circulosiete.curso.testing.clase03.repository.AccountRepository;
import com.circulosiete.curso.testing.clase03.service.Notifier;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class TransferService {
    private final Notifier notifier;
    private final AccountRepository accountRepository;

    public TransferService(Notifier notifier, AccountRepository accountRepository) {
        this.notifier = notifier;
        this.accountRepository = accountRepository;
    }

    public Optional<Transfer> transfer(
            UUID from,
            UUID to,
            BigDecimal amountToTransfer
    ) {
        var maybeAccountFrom = this.accountRepository.findById(from);
        var maybeAccountTo = this.accountRepository.findById(to);

        return maybeAccountFrom
                .flatMap(fromAccount ->
                        maybeAccountTo.map(
                                toAccount ->
                                        performTransfer(
                                                amountToTransfer,
                                                fromAccount,
                                                toAccount)));
    }

    private Transfer performTransfer(
            BigDecimal amountToTransfer,
            Account fromAccount,
            Account toAccount
    ) {
        fromAccount.withdraw(amountToTransfer);
        toAccount.deposit(amountToTransfer);

        final var transfer = new Transfer(
                UUID.randomUUID(),
                fromAccount.getUuid(),
                toAccount.getUuid(),
                amountToTransfer,
                LocalDateTime.now()
        );
        this.accountRepository.save(fromAccount);
        this.accountRepository.save(toAccount);
        this.triggerTransferEvent(new TransferCreated(transfer));
        return transfer;
    }

    private void triggerTransferEvent(TransferCreated transferCreated) {
        var amount = transferCreated.transfer().amount();

        DecimalFormat df = new DecimalFormat("#0.00");
        String formatted = df.format(amount);

        notifier.notify(
                transferCreated.transfer().from().toString(),
                "Transfer sent: " + formatted);
        notifier.notify(
                transferCreated.transfer().to().toString(),
                "Transfer received: " + formatted);
    }
}
