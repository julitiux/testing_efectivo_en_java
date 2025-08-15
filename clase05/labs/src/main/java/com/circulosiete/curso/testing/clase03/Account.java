package com.circulosiete.curso.testing.clase03;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {
    private final UUID uuid;
    private BigDecimal balance;

    public Account(UUID uuid, BigDecimal initialBalance) {
        this.uuid = uuid;
        this.balance = initialBalance;
    }

    public UUID getUuid() {
        return uuid;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void withdraw(BigDecimal amount) {
        var comparison = getBalance().compareTo(amount);
        if (comparison < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
}
