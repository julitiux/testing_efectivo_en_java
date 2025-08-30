package com.circulosiete.curso.minibank.payments.adapter.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentDao extends JpaRepository<PaymentJpa, UUID> {
    Optional<PaymentJpa> findByRequestId(String requestId);

    Optional<PaymentJpa> findByExternalRef(String externalRef);
}
