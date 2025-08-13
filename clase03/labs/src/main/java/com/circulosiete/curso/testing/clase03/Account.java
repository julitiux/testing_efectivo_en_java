package com.circulosiete.curso.testing.clase03;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {
  private final UUID uuid;
  private final BigDecimal balance;

  public Account(UUID uuid, BigDecimal fromInitialBalance) {
    this.uuid = uuid;
    this.balance = fromInitialBalance;
  }

  public UUID getUuid() {
    return uuid;
  }

  public BigDecimal getBalance() {
    return balance;
  }
}
