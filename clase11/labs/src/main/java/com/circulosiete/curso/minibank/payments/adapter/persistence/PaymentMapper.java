package com.circulosiete.curso.minibank.payments.adapter.persistence;

import com.circulosiete.curso.minibank.model.Money;
import com.circulosiete.curso.minibank.payments.domain.Payment;
import java.util.Objects;

public final class PaymentMapper {
    private PaymentMapper() {
    }

    public static Payment toDomain(PaymentJpa paymentJpa) {
        final var payment = new Payment(
            paymentJpa.getId(),
            paymentJpa.getRequestId(),
            paymentJpa.getFromAccountId(),
            paymentJpa.getToBankAccount(),
            paymentJpa.getToBankId(),
            Money.of(paymentJpa.getAmount(), paymentJpa.getCurrency())
        );
        payment.setCreatedAt(paymentJpa.getCreatedAt());
        payment.setUpdatedAt(paymentJpa.getUpdatedAt());
        payment.setExternalRef(paymentJpa.getExternalRef());
        payment.setState(paymentJpa.getState());
        return payment;
    }

    public static void copyToJpa(Payment payment, PaymentJpa paymentJpa) {
        paymentJpa.setId(payment.getId());
        paymentJpa.setRequestId(payment.getRequestId());
        paymentJpa.setFromAccountId(payment.getFromAccountId());
        paymentJpa.setToBankAccount(payment.getToBankAccount());
        paymentJpa.setToBankId(payment.getToBankId());
        paymentJpa.setCurrency(payment.getAmount().getCurrency());
        paymentJpa.setAmount(payment.getAmount().getAmount());
        paymentJpa.setState(payment.getState());
        paymentJpa.setExternalRef(payment.getExternalRef());
        paymentJpa.setUpdatedAt(payment.getUpdatedAt());
        if (Objects.nonNull(payment.getCreatedAt())) {
            paymentJpa.setCreatedAt(payment.getCreatedAt());
        }
    }
}

