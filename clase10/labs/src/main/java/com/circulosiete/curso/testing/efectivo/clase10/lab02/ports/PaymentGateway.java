package com.circulosiete.curso.testing.efectivo.clase10.lab02.ports;

import java.math.BigDecimal;

public interface PaymentGateway {
    boolean charge(String customerId, BigDecimal amount);
}
