package com.circulosiete.curso.minibank.payments.ports.in;

import java.time.Instant;
import java.util.UUID;

public interface GetPaymentStatus {
    enum ExternalStatus {
        UNKNOWN,
        ACCEPTED,
        PENDING,
        REJECTED,
        SETTLED
    }

    record Query(UUID paymentId) {
    }

    record Result(
        UUID paymentId,
        String state,
        ExternalStatus externalStatus,
        Instant updatedAt
    ) {
    }

    Result handle(Query query);
}
