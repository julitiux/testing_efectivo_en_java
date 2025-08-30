package com.circulosiete.curso.minibank.payments.ports.out;

public interface PaymentRailPort {
    record TransferRequest(
        String requestId,
        String debtorAccount,
        String creditorAccount,
        String creditorBankId,
        String currency,
        String amount,
        String purpose
    ) {
    }

    record TransferResponse(
        String externalRef,
        boolean accepted,
        String message
    ) {
    }

    record StatusResponse(
        String externalRef,
        String status,
        String rawPayload
    ) {
    }

    TransferResponse send(TransferRequest req);           // pacs.008 / SPEI / ACH, etc.

    StatusResponse status(String externalRef);          // consulta estado en el riel

    boolean cancel(String externalRef);          // si el riel lo soporta
}
