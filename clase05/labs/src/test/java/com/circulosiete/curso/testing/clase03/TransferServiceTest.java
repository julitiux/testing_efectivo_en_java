package com.circulosiete.curso.testing.clase03;

import com.circulosiete.curso.testing.clase03.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {
    @Mock
    AccountRepository accountRepository;
    TransferService underTest;

    @BeforeEach
    void setUp() {
        underTest = new TransferService(accountRepository);
    }

    @Test
    void transfers_between_accounts_and_records_event() {
        var fromInitialBalance = BigDecimal.valueOf(50);
        var toInitialBalance = BigDecimal.valueOf(10);
        var amountToTransfer = BigDecimal.valueOf(20);
        var expectedFromAccountBalanceAfterTransfer = BigDecimal.valueOf(30);
        var expectedToAccountBalanceAfterTransfer = BigDecimal.valueOf(30);
        var fromId = UUID.randomUUID();
        var toId = UUID.randomUUID();
        var from = new Account(fromId, fromInitialBalance);
        var to = new Account(toId, toInitialBalance);

        when(accountRepository.findById(fromId))
                .thenReturn(from);
        when(accountRepository.findById(toId))
                .thenReturn(to);

        assertEquals(from.getBalance(), fromInitialBalance);
        assertEquals(to.getBalance(), toInitialBalance);

        var initialEvents = this.underTest.getEvents();
        assertNotNull(initialEvents);
        assertInstanceOf(List.class, initialEvents);
        assertTrue(initialEvents.isEmpty());

        var result = this.underTest.transfer(from.getUuid(), to.getUuid(), amountToTransfer);

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
        assertEquals(1, afterTransferEvents.size());
    }
}


