package com.circulosiete.curso.testing.efectivo.clase10.lab02;

import com.circulosiete.curso.testing.efectivo.clase10.lab02.ports.EmailSender;
import com.circulosiete.curso.testing.efectivo.clase10.lab02.ports.InventoryService;
import com.circulosiete.curso.testing.efectivo.clase10.lab02.ports.PaymentGateway;

public class OrderService {

    private final PaymentGateway payment;
    private final InventoryService inventory;
    private final EmailSender email;

    public OrderService(
        PaymentGateway payment,
        InventoryService inventory,
        EmailSender email
    ) {
        this.payment = payment;
        this.inventory = inventory;
        this.email = email;
    }

    public boolean placeOrder(OrderRequest orderRequest) {
        if (orderRequest == null) {
            throw new IllegalArgumentException("OrderRequest is null");
        }
        if (orderRequest.units() <= 0) {
            return false;
        }

        boolean inStock = inventory.hasStock(
            orderRequest.sku(),
            orderRequest.units()
        );
        if (!inStock) {
            return false;
        }

        var total = orderRequest.total();
        if (total.signum() <= 0) {
            return false;
        }

        boolean paid = payment.charge(
            orderRequest.customerId(),
            total
        );
        if (!paid) {
            return false;
        }

        inventory.reserve(
            orderRequest.sku(),
            orderRequest.units()
        );
        email.sendOrderConfirmation(
            orderRequest.customerEmail(),
            orderRequest.orderId()
        );
        return true;
    }
}
