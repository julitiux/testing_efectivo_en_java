package com.circulosiete.curso.testing.efectivo.clase10.lab02.ports;

public interface EmailSender {
    void sendOrderConfirmation(String email, String orderId);
}
