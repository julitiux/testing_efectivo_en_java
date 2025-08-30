package com.circulosiete.curso.minibank.repository;

import com.circulosiete.curso.minibank.model.TransferRecord;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRecordRepository extends JpaRepository<TransferRecord, UUID> {
    Optional<TransferRecord> findByRequestId(String requestId);
}
