package com.circulosiete.curso.testing.clase03;

import com.circulosiete.curso.testing.clase03.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransferService {
    private final List<TransferCreated> events;
    private final AccountRepository accountRepository;

    public TransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        this.events = new ArrayList<>();
    }

    public List<TransferCreated> getEvents() {
        return events;
    }

    public Transfer transfer(
            UUID from,
            UUID to,
            BigDecimal amountToTransfer
    ) {
        var accountFrom = this.accountRepository.findById(from);
        var accountTo = this.accountRepository.findById(to);

        accountFrom.withdraw(amountToTransfer);
        accountTo.deposit(amountToTransfer);

        Transfer transfer = new Transfer(
                UUID.randomUUID(),
                accountFrom.getUuid(),
                accountTo.getUuid(),
                amountToTransfer,
                LocalDateTime.now()
        );
        this.triggerTransferEvent(new TransferCreated(transfer));

        return transfer;
    }

    private void triggerTransferEvent(TransferCreated transferCreated) {
        // logica aqui para disparar el evento
        this.events.add(transferCreated);
    }
}
