package com.circulosiete.curso.minibank.payments.ports.out;

import com.circulosiete.curso.minibank.payments.domain.Payment;
import com.circulosiete.curso.minibank.payments.ports.in.InitiateInterbankTransfer;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Optional<Payment> findByRequestId(String requestId);

    Optional<Payment> findById(UUID id);

    Optional<Payment> findByExternalRef(String externalRef);

    Payment save(Payment p);

    Payment create(
        InitiateInterbankTransfer.Command command
    );

}
