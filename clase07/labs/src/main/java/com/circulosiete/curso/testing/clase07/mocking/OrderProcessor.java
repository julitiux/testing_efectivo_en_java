package com.circulosiete.curso.testing.clase07.mocking;

public class OrderProcessor {
    private final PaymentService paymentService;

    public OrderProcessor(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public boolean processOrder(int amount) {
        return paymentService.pay(amount);
    }
}
