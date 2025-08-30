package com.circulosiete.curso.minibank.payments.app;


import com.circulosiete.curso.minibank.model.Money;
import com.circulosiete.curso.minibank.payments.domain.Payment;
import com.circulosiete.curso.minibank.payments.domain.PaymentState;
import com.circulosiete.curso.minibank.payments.ports.in.InitiateInterbankTransfer;
import com.circulosiete.curso.minibank.payments.ports.out.AccountLedgerPort;
import com.circulosiete.curso.minibank.payments.ports.out.AmlPort;
import com.circulosiete.curso.minibank.payments.ports.out.BankDirectoryPort;
import com.circulosiete.curso.minibank.payments.ports.out.PaymentRailPort;
import com.circulosiete.curso.minibank.payments.ports.out.PaymentRailRegistry;
import com.circulosiete.curso.minibank.payments.ports.out.PaymentRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitiateInterbankTransferServiceTest {

    @Mock
    PaymentRepository repo;
    @Mock
    PaymentRailRegistry railRegistry;
    @Mock
    AccountLedgerPort ledger;
    @Mock
    BankDirectoryPort directory;
    @Mock
    AmlPort aml;
    @Mock
    PaymentRailPort rail;

    InitiateInterbankTransferService service;

    @BeforeEach
    void setUp() {
        service = new InitiateInterbankTransferService(repo, railRegistry, ledger, directory, aml);
    }

    private InitiateInterbankTransfer.Command cmd() {
        return new InitiateInterbankTransfer.Command(
            "req-123",
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            "002010077777777771", // CLABE válida
            "002",
            new BigDecimal("123.45"),
            "MXN",
            "demo",
            "ref"
        );
    }

    private Payment newPaymentWithId(UUID id) {
        var money = Money.of(new BigDecimal("123.45"), "MXN");
        Payment payment = new Payment(
            id,
            "req-123",
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            "002010077777777771",
            "002",
            money
        );
        payment.setState(PaymentState.INITIATED);
        return payment;
    }

    @Test
    void should_return_existing_payment_on_idempotency() {
        var existing = newPaymentWithId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        when(repo.findByRequestId("req-123")).thenReturn(Optional.of(existing));

        var id = service.handle(cmd());

        assertThat(id)
            .isEqualTo(existing.getId());
        verifyNoInteractions(directory, aml, ledger, railRegistry);
    }

    @Test
    void happy_path_should_validate_hold_send_and_mark_sent() {
        when(repo.findByRequestId("req-123"))
            .thenReturn(Optional.empty());
        when(directory.isValidAccount(anyString(), anyString(), anyString()))
            .thenReturn(true);
        when(aml.passesAml(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(true);
        when(ledger.hold(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("HOLD-1");

        var created = newPaymentWithId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        var command = any(InitiateInterbankTransfer.Command.class);
        var transferResponse = new PaymentRailPort.TransferResponse(
            "SPEI-req-123",
            true,
            "OK"
        );

        when(repo.create(command))
            .thenReturn(created);
        when(railRegistry.resolve(anyString(), anyString()))
            .thenReturn(rail);
        when(rail.send(any()))
            .thenReturn(transferResponse);

        var id = service.handle(cmd());

        assertThat(id)
            .isEqualTo(created.getId());

        // Verificamos que se marcó como SENT y se guardó
        var pCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(repo)
            .save(pCaptor.capture());
        assertThat(pCaptor.getValue().getState())
            .isEqualTo(PaymentState.SENT);
        assertThat(pCaptor.getValue().getExternalRef())
            .isEqualTo("SPEI-req-123");
    }

    @Test
    void when_rail_rejects_should_release_hold_mark_failed_and_throw() {
        when(repo.findByRequestId("req-123"))
            .thenReturn(Optional.empty());
        when(directory.isValidAccount(anyString(), anyString(), anyString()))
            .thenReturn(true);
        when(aml.passesAml(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(true);
        when(ledger.hold(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("HOLD-1");

        var created = newPaymentWithId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        var command = any(InitiateInterbankTransfer.Command.class);
        when(repo.create(command))
            .thenReturn(created);
        when(railRegistry.resolve(anyString(), anyString()))
            .thenReturn(rail);
        when(rail.send(any()))
            .thenReturn(new PaymentRailPort.TransferResponse(null, false, "Rejected by rail"));

        assertThatThrownBy(() -> service.handle(cmd()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Riel rechazó: Rejected by rail");

        verify(ledger).release("HOLD-1");

        var pCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(repo).save(pCaptor.capture());
        assertThat(pCaptor.getValue().getState())
            .isEqualTo(PaymentState.FAILED);
    }

    @Test
    void should_fail_when_directory_validation_fails() {
        when(repo.findByRequestId("req-123"))
            .thenReturn(Optional.empty());
        when(directory.isValidAccount(anyString(), anyString(), anyString()))
            .thenReturn(false);

        assertThatThrownBy(() -> service.handle(cmd()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cuenta destino inválida");
        verifyNoInteractions(aml, ledger, railRegistry);
    }

    @Test
    void should_fail_when_aml_fails() {
        when(repo.findByRequestId("req-123"))
            .thenReturn(Optional.empty());
        when(directory.isValidAccount(anyString(), anyString(), anyString()))
            .thenReturn(true);
        when(aml.passesAml(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(false);

        assertThatThrownBy(() -> service.handle(cmd()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("AML fail");
        verifyNoInteractions(ledger, railRegistry);
    }
}

