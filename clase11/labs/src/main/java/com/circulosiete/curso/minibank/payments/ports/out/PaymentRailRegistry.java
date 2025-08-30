package com.circulosiete.curso.minibank.payments.ports.out;

public interface PaymentRailRegistry {
    PaymentRailPort resolve(String toBankId, String currency);
}
