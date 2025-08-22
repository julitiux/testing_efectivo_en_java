package com.circulosiete.curso.minibank.repository;

import com.circulosiete.curso.minibank.model.ProcessedOperation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedOperationRepository extends JpaRepository<ProcessedOperation, UUID> {
    Optional<ProcessedOperation> findByAccountIdAndRequestId(UUID accountId, String requestId);
}
