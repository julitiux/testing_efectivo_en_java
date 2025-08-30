package com.circulosiete.curso.minibank.payments.app;

import com.circulosiete.curso.minibank.payments.domain.PaymentState;
import com.circulosiete.curso.minibank.payments.ports.in.InitiateInterbankTransfer;
import com.circulosiete.curso.minibank.payments.ports.out.AccountLedgerPort;
import com.circulosiete.curso.minibank.payments.ports.out.AmlPort;
import com.circulosiete.curso.minibank.payments.ports.out.BankDirectoryPort;
import com.circulosiete.curso.minibank.payments.ports.out.PaymentRailPort;
import com.circulosiete.curso.minibank.payments.ports.out.PaymentRailRegistry;
import com.circulosiete.curso.minibank.payments.ports.out.PaymentRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InitiateInterbankTransferService implements InitiateInterbankTransfer {

    private final PaymentRepository paymentRepository;           // interfaz del dominio
    private final PaymentRailRegistry railRegistry; // selecciona riel por país/moneda/banco
    private final AccountLedgerPort ledger;
    private final BankDirectoryPort directory;
    private final AmlPort aml;

    @Override
    public UUID handle(Command command) {
        // 1) Idempotencia: ¿ya existe un Payment con requestId?
        var existing = paymentRepository.findByRequestId(command.requestId());
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        // 2) Validaciones síncronas
        boolean validAccount = directory.isValidAccount(
            command.toBankAccount(),
            command.toBankId(),
            command.currency()
        );

        if (!validAccount) {
            throw new IllegalArgumentException("Cuenta destino inválida");
        }

        boolean passesAml = aml.passesAml(
            command.fromAccountId().toString(),
            command.toBankAccount(),
            command.amount().toPlainString(),
            command.currency()
        );

        if (!passesAml) {
            throw new IllegalStateException("AML fail");
        }

        // 3) Reservar fondos (hold)
        var holdRef = ledger.hold(
            command.requestId(),
            command.fromAccountId().toString(),
            command.currency(),
            command.amount().toPlainString(),
            "Interbank transfer"
        );

        // 4) Crear Payment en estado INITIATED
        var payment = paymentRepository.create(command);
        System.out.println("Created payment: " + payment);

        // 5) Seleccionar riel y enviar
        var rail = railRegistry.resolve(command.toBankId(), command.currency());
        var resp = rail.send(new PaymentRailPort.TransferRequest(
            command.requestId(),
            payment.getFromAccountId().toString(),
            command.toBankAccount(),
            command.toBankId(),
            command.currency(),
            command.amount().toPlainString(),
            command.purpose()
        ));

        if (!resp.accepted()) {
            ledger.release(holdRef);
            payment.markFailed(resp.message());
            paymentRepository.save(payment);
            throw new IllegalStateException("Riel rechazó: " + resp.message());
        }

        // 6) Marcar enviado y postear (asiento) -> usando Outbox/Saga según acople temporal del riel
        payment.markSent(resp.externalRef());
        paymentRepository.save(payment);

        // Aquí puedes elegir:
        // a) Postear de inmediato y compensar si falla liquidación
        // b) Postear al liquidar (recomendado si el riel notifica asincrónicamente)
        // Ejemplo: post diferido en listener del callback de liquidación.

        return payment.getId();
    }
}
