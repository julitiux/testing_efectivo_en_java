package com.circulosiete.curso.testing.clase03.aaa;

import com.circulosiete.curso.testing.clase03.Account;
import com.circulosiete.curso.testing.clase03.TransferService;
import com.circulosiete.curso.testing.clase03.repository.AccountRepository;
import com.circulosiete.curso.testing.clase03.service.Notifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AAATransferServiceTest {
    @Mock
    AccountRepository repo;
    @Mock
    Notifier notifier;
    @InjectMocks
    TransferService service;
    @Captor
    ArgumentCaptor<Account> accountCaptor;

    @Test
    void transfer_shouldMoveMoneyAndNotify_bothParties() {
        // Arrange: escenario y “stubs”
        var fromId = UUID.randomUUID();
        var toId = UUID.randomUUID();
        var from = new Account(fromId, new BigDecimal("100.00"));
        var to = new Account(toId, new BigDecimal("50.00"));

        when(repo.findById(fromId)).thenReturn(Optional.of(from));
        when(repo.findById(toId)).thenReturn(Optional.of(to));

        // Act: una sola acción principal
        service.transfer(fromId, toId, new BigDecimal("30.00"));

        // Assert: verificaciones del estado y de interacciones
        assertAll(
                () -> assertEquals(new BigDecimal("70.00"), from.getBalance(), "debit correcto"),
                () -> assertEquals(new BigDecimal("80.00"), to.getBalance(), "credit correcto")
        );

        // Verifica que se guardaron ambas cuentas con los nuevos saldos
        verify(repo, times(2)).save(accountCaptor.capture());
        var saved = accountCaptor.getAllValues();
        assertTrue(saved.stream().anyMatch(a -> a.getUuid().equals(fromId) && a.getBalance().compareTo(new BigDecimal("70.00")) == 0));
        assertTrue(saved.stream().anyMatch(a -> a.getUuid().equals(toId) && a.getBalance().compareTo(new BigDecimal("80.00")) == 0));

        // Verifica notificaciones
        verify(notifier).notify(fromId.toString(), "Transfer sent: 30.00");
        verify(notifier).notify(toId.toString(), "Transfer received: 30.00");

        // Asegura que no hubo interacciones extra
        verifyNoMoreInteractions(repo, notifier);
    }

    @Test
    void transfer_shouldFail_whenInsufficientFunds_andNotPersistOrNotify() {
        // Arrange
        var fromId = UUID.randomUUID();
        var toId = UUID.randomUUID();
        var from = new Account(fromId, new BigDecimal("10.00"));
        var to = new Account(toId, new BigDecimal("50.00"));
        when(repo.findById(fromId)).thenReturn(Optional.of(from));
        when(repo.findById(toId)).thenReturn(Optional.of(to));

        // Act + Assert: excepción esperada (AAA puede combinarse aquí)
        var ex = assertThrows(IllegalStateException.class,
                () -> service.transfer(fromId, toId, new BigDecimal("30.00")));

        assertEquals("Insufficient funds", ex.getMessage());

        // Assert (interacciones negativas): no se guarda ni se notifica
        verify(repo, never()).save(any());
        verify(notifier, never()).notify(anyString(), anyString());
        verifyNoMoreInteractions(repo, notifier);
    }
}
