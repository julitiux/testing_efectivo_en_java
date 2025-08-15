package com.circulosiete.curso.testing.clase03;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TransferService {
  public List<TransferCreated> getEvents() {
    return List.of();
  }

  public Transfer transfer(Account from, Account to, BigDecimal amountToTransfer) {
    var comparison = from.getBalance().compareTo(amountToTransfer);
    if (comparison < 0) {
      throw new IllegalStateException("Insufficient funds");
    }

    var result =  new Transfer(
      UUID.randomUUID(),
      from.getUuid(),
      to.getUuid(),
      amountToTransfer,
      LocalDateTime.now());
    return result;
  }
}
