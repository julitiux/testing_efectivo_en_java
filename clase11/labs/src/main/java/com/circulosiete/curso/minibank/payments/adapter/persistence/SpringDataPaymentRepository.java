package com.circulosiete.curso.minibank.payments.adapter.persistence;

import com.circulosiete.curso.minibank.payments.domain.Payment;
import com.circulosiete.curso.minibank.payments.domain.PaymentState;
import com.circulosiete.curso.minibank.payments.ports.in.InitiateInterbankTransfer;
import com.circulosiete.curso.minibank.payments.ports.out.PaymentRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SpringDataPaymentRepository implements PaymentRepository {
    private final JpaPaymentDao dao;

    @Override
    public Optional<Payment> findByRequestId(String requestId) {
        return dao.findByRequestId(requestId)
            .map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return dao.findById(id)
            .map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByExternalRef(String externalRef) {
        return dao.findByExternalRef(externalRef)
            .map(PaymentMapper::toDomain);
    }

    @Override
    public Payment save(Payment p) {
        var j = p.getId() != null ? dao.findById(p.getId()).orElse(new PaymentJpa()) : new PaymentJpa();
        PaymentMapper.copyToJpa(p, j);
        return PaymentMapper.toDomain(dao.save(j));
    }

    @Override
    public Payment create(InitiateInterbankTransfer.Command command) {

        var paymentJpa = new PaymentJpa();
        paymentJpa.setRequestId(command.requestId());
        paymentJpa.setFromAccountId(command.fromAccountId());
        paymentJpa.setToBankAccount(command.toBankAccount());
        paymentJpa.setToBankId(command.toBankId());
        paymentJpa.setCurrency(command.currency());
        paymentJpa.setAmount(command.amount());

        return PaymentMapper.toDomain(dao.save(paymentJpa));
    }
}
