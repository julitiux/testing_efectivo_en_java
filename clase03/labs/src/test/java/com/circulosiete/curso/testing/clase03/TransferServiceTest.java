package com.circulosiete.curso.testing.clase03;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TransferServiceTest {

  TransferService underTest;

  void setUp() {
    // Initialize the TransferService and any required dependencies
    underTest = new TransferService();
  }

  @Test
  void transfers_between_account_and_records_event() {

    var fromInitialBalance = BigDecimal.valueOf(50);
    var toInitialBalance = BigDecimal.valueOf(10);
    var amountToTransfer = BigDecimal.valueOf(20);
    var expectedFromAccountBalanceAfterTransfer = BigDecimal.valueOf(30);
    var expectedToAccountBalanceAfterTransfer = BigDecimal.valueOf(30);

    var from = new Account(UUID.randomUUID(), fromInitialBalance);
    var to = new Account(UUID.randomUUID(), toInitialBalance);

    assertEquals(from.getBalance(), fromInitialBalance);
    assertEquals(to.getBalance(), toInitialBalance);

    var initialEvents = this.underTest.getEvents();
    assertNotNull(initialEvents);
    assertInstanceOf(List.class, initialEvents);
    assertTrue(initialEvents.isEmpty());

    var result = this.underTest.transfer(from, to, BigDecimal.valueOf(10));

    assertNotNull(result);
    assertInstanceOf(Transfer.class, result);
    assertNotNull(result.id());
    assertInstanceOf(UUID.class, result.id());
    assertNotNull(result.from());
    assertInstanceOf(UUID.class, result.from());
    assertNotNull(result.to());
    assertInstanceOf(UUID.class, result.to());
    assertNotNull(result.amount());
    assertInstanceOf(BigDecimal.class, result.amount());
    assertNotNull(result.when());
    assertInstanceOf(LocalDateTime.class, result.when());

    assertEquals(from.getBalance(), expectedFromAccountBalanceAfterTransfer);
    assertEquals(to.getBalance(), expectedToAccountBalanceAfterTransfer);

    assertEquals(
      from.getBalance(),
      fromInitialBalance.subtract(amountToTransfer)
    );
    assertEquals(
      to.getBalance(),
      toInitialBalance.add(amountToTransfer)
    );

    var afterTransferEvents = this.underTest.getEvents();
    assertFalse(afterTransferEvents.isEmpty());
    assertEquals(afterTransferEvents.size(), 1);
  }
}
