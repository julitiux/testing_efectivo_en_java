package com.circulosiete.curso.minibank.payments.ports.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface InitiateInterbankTransfer {
    record Command(
        String requestId,              // idem-potency key del cliente
        UUID fromAccountId,
        String toBankAccount,          // CLABE/IBAN
        String toBankId,               // BIC/Institution Id
        BigDecimal amount,
        String currency,
        String purpose,
        String paymentReference
    ) {
    }

    UUID handle(Command command);
}
