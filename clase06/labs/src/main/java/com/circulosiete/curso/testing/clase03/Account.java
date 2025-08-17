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

    void withdraw(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        balance = balance.subtract(amount);
    }

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
}
