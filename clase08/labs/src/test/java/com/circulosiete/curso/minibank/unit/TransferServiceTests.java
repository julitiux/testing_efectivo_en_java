package com.circulosiete.curso.minibank.unit;

import com.circulosiete.curso.minibank.commands.FundsTransferred;
import com.circulosiete.curso.minibank.commands.TransferFunds;
import com.circulosiete.curso.minibank.model.Account;
import com.circulosiete.curso.minibank.model.AccountType;
import com.circulosiete.curso.minibank.model.Money;
import com.circulosiete.curso.minibank.model.TransferRecord;
import com.circulosiete.curso.minibank.repository.AccountRepository;
import com.circulosiete.curso.minibank.repository.TransferRecordRepository;
import com.circulosiete.curso.minibank.service.TransferService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTests {
    @Mock
    AccountRepository accounts;
    @Mock
    TransferRecordRepository transfers;
    @Mock
    ApplicationEventPublisher events;

    @InjectMocks
    TransferService service;

    UUID fromId;
    UUID toId;
    Account from;
    Account to;

    @BeforeEach
    void setUp() {
        fromId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        toId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        from = Account.open("ACC-FROM", AccountType.CURRENT, UUID.randomUUID(), Money.of(new BigDecimal("1000.00"), "MXN"));
        from.setId(fromId);
        to = Account.open("ACC-TO", AccountType.CURRENT, UUID.randomUUID(), Money.of(new BigDecimal("100.00"), "MXN"));
        to.setId(toId);
    }

    @Test
    void happy_path_debits_credits_completes_record_and_publishes_event_with_stable_lock_order() {
        // given
        var cmd = new TransferFunds(fromId, toId, new BigDecimal("250.00"), "MXN", "req-1");

        // 1) idempotency insert OK
        when(transfers.saveAndFlush(any(TransferRecord.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // 2) locking in order: fromId < toId; mockea llamadas en ese orden
        when(accounts.lockByIdForUpdate(fromId))
            .thenReturn(Optional.of(from));
        when(accounts.lockByIdForUpdate(toId))
            .thenReturn(Optional.of(to));

        // 3) findByRequestId devolverá un record con ID (como si la BD lo hubiera generado)
        var recId = UUID.randomUUID();
        var savedRec = TransferRecord.builder()
            .id(recId)
            .requestId(cmd.requestId())
            .fromAccountId(fromId)
            .toAccountId(toId)
            .amount(cmd.amount())
            .currency(cmd.currency())
            .status(TransferRecord.Status.PENDING)
            .createdAt(Instant.now())
            .build();
        when(transfers.findByRequestId("req-1"))
            .thenReturn(Optional.of(savedRec));

        // when
        var resultId = service.handle(cmd);

        // then: balances
        assertThat(from.getBalance().getAmount())
            .isEqualByComparingTo("750.00");
        assertThat(to.getBalance().getAmount())
            .isEqualByComparingTo("350.00");

        // se guardan ambas cuentas
        verify(accounts).save(from);
        verify(accounts).save(to);

        // el TransferRecord se completa y se persiste
        var recCaptor = ArgumentCaptor.forClass(TransferRecord.class);
        verify(transfers).save(recCaptor.capture());
        assertThat(recCaptor.getValue().getStatus())
            .isEqualTo(TransferRecord.Status.COMPLETED);
        assertThat(recCaptor.getValue().getCompletedAt())
            .isNotNull();

        // evento publicado con datos correctos
        var evtCaptor = ArgumentCaptor.forClass(FundsTransferred.class);
        verify(events).publishEvent(evtCaptor.capture());
        var evt = evtCaptor.getValue();
        assertThat(evt.transferId())
            .isEqualTo(recId);
        assertThat(evt.fromAccountId())
            .isEqualTo(fromId);
        assertThat(evt.toAccountId())
            .isEqualTo(toId);
        assertThat(evt.amount())
            .isEqualByComparingTo("250.00");
        assertThat(evt.currency())
            .isEqualTo("MXN");

        // retorna el ID de la transferencia
        assertThat(resultId).isEqualTo(recId);

        // locking en orden estable (de menor a mayor UUID)
        InOrder inOrder = inOrder(accounts);
        inOrder.verify(accounts).lockByIdForUpdate(fromId);
        inOrder.verify(accounts).lockByIdForUpdate(toId);

        // idempotency insert con datos del comando (status PENDING)
        var insertCaptor = ArgumentCaptor.forClass(TransferRecord.class);
        verify(transfers).saveAndFlush(insertCaptor.capture());
        var inserted = insertCaptor.getValue();
        assertThat(inserted.getRequestId()).isEqualTo("req-1");
        assertThat(inserted.getStatus()).isEqualTo(TransferRecord.Status.PENDING);
        assertThat(inserted.getFromAccountId()).isEqualTo(fromId);
        assertThat(inserted.getToAccountId()).isEqualTo(toId);
        assertThat(inserted.getAmount()).isEqualByComparingTo("250.00");
        assertThat(inserted.getCurrency()).isEqualTo("MXN");
    }

    @Test
    void duplicate_request_throws_AlreadyProcessedException_and_does_not_change_balances_or_publish_event() {
        // given
        var cmd = new TransferFunds(fromId, toId, new BigDecimal("50.00"), "MXN", "dup-1");
        when(transfers.saveAndFlush(any(TransferRecord.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate"));

        // when / then
        assertThatThrownBy(() -> service.handle(cmd))
            .isInstanceOf(TransferService.AlreadyProcessedException.class);

        verifyNoInteractions(events);
        verify(accounts, never()).lockByIdForUpdate(any());
        verify(accounts, never()).save(any());
        // balances intactos
        assertThat(from.getBalance().getAmount()).isEqualByComparingTo("1000.00");
        assertThat(to.getBalance().getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void account_not_found_throws_IllegalArgumentException() {
        // given: idempotency OK
        when(transfers.saveAndFlush(any(TransferRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        // el menor UUID es fromId; devolvemos from OK y to NOT FOUND
        when(accounts.lockByIdForUpdate(fromId)).thenReturn(Optional.of(from));
        when(accounts.lockByIdForUpdate(toId)).thenReturn(Optional.empty());

        var cmd = new TransferFunds(fromId, toId, new BigDecimal("10.00"), "MXN", "nf-1");

        // when / then
        assertThatThrownBy(() -> service.handle(cmd))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Account not found");

        verify(events, never()).publishEvent(any());
        verify(accounts, never()).save(any());
        // No se completó el record (no hay transfers.save(...))
        verify(transfers, never()).save(any(TransferRecord.class));
    }

    @Test
    void currency_mismatch_throws_IllegalArgumentException_and_no_save() {
        // given
        when(transfers.saveAndFlush(any(TransferRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        // to en USD para forzar mismatch
        to.setBalance(Money.of(new BigDecimal("100.00"), "USD"));
        when(accounts.lockByIdForUpdate(fromId)).thenReturn(Optional.of(from));
        when(accounts.lockByIdForUpdate(toId)).thenReturn(Optional.of(to));

        var cmd = new TransferFunds(fromId, toId, new BigDecimal("10.00"), "MXN", "cur-1");

        // when / then
        assertThatThrownBy(() -> service.handle(cmd))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Currency mismatch");

        verify(accounts, never()).save(any());
        verify(events, never()).publishEvent(any());
        verify(transfers, never()).save(any(TransferRecord.class));
    }

    @Test
    void insufficient_funds_throws_IllegalStateException_and_no_account_saves() {
        // given
        when(transfers.saveAndFlush(any(TransferRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accounts.lockByIdForUpdate(fromId)).thenReturn(Optional.of(from));
        when(accounts.lockByIdForUpdate(toId)).thenReturn(Optional.of(to));

        var cmd = new TransferFunds(fromId, toId, new BigDecimal("2000.00"), "MXN", "nsf-1");

        // when / then
        assertThatThrownBy(() -> service.handle(cmd))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Insufficient funds");

        verify(accounts, never()).save(any());           // debit falló antes de persistir
        verify(events, never()).publishEvent(any());     // no hay evento
        verify(transfers, never()).save(any());          // no completamos el record
    }
}
