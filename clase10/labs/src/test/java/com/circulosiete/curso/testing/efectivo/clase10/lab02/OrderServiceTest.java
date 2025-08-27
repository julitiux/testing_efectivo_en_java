package com.circulosiete.curso.testing.efectivo.clase10.lab02;


import com.circulosiete.curso.testing.efectivo.clase10.lab02.ports.EmailSender;
import com.circulosiete.curso.testing.efectivo.clase10.lab02.ports.InventoryService;
import com.circulosiete.curso.testing.efectivo.clase10.lab02.ports.PaymentGateway;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    PaymentGateway payment;
    @Mock
    InventoryService inventory;
    @Mock
    EmailSender email;

    @InjectMocks
    OrderService service;

    @Captor
    ArgumentCaptor<BigDecimal> amountCaptor;

    OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest(
            "ORD-1",
            "CUST-9",
            "user@domain.tld",
            "SKU-ABC",
            2,
            new BigDecimal("10.50")
        );
    }

    @Test
    void placeOrder_success_happy_path() {
        when(inventory.hasStock("SKU-ABC", 2))
            .thenReturn(true);
        when(payment.charge(eq("CUST-9"), any()))
            .thenReturn(true);

        var ok = service.placeOrder(orderRequest);

        assertTrue(ok, "Debe completar la orden");
        // Capturamos el monto exacto para matar mutaciones en cálculos
        verify(payment).charge(eq("CUST-9"), amountCaptor.capture());
        assertEquals(new BigDecimal("21.00"), amountCaptor.getValue());

        // Verificamos interacciones exactas (matan mutaciones en flujo)
        verify(inventory)
            .reserve("SKU-ABC", 2);
        verify(email)
            .sendOrderConfirmation("user@domain.tld", "ORD-1");
    }

    @Test
    void placeOrder_fails_when_no_stock() {
        when(inventory.hasStock("SKU-ABC", 2))
            .thenReturn(false);

        var ok = service.placeOrder(orderRequest);

        assertFalse(ok);
        verifyNoInteractions(email);
        verify(payment, times(0))
            .charge(anyString(), any());
        verify(inventory, times(0))
            .reserve(anyString(), anyInt());
    }

    @Test
    void placeOrder_fails_when_payment_declined() {
        when(inventory.hasStock("SKU-ABC", 2))
            .thenReturn(true);
        when(payment.charge(eq("CUST-9"), any()))
            .thenReturn(false);

        var ok = service.placeOrder(orderRequest);

        assertFalse(ok);
        verify(inventory, times(0))
            .reserve(anyString(), anyInt());
        verifyNoInteractions(email);
    }

    @Test
    void placeOrder_fails_when_units_non_positive() {
        var bad = new OrderRequest(
            "X",
            "C",
            "mail@x",
            "SKU",
            0,
            new BigDecimal("5")
        );
        boolean ok = service.placeOrder(bad);
        assertFalse(ok);
        verifyNoInteractions(payment, inventory, email);
    }

    @Test
    void placeOrder_fails_when_total_not_positive() {
        var bad = new OrderRequest(
            "X",
            "C",
            "mail@x",
            "SKU",
            2,
            BigDecimal.ZERO
        );
        when(inventory.hasStock("SKU", 2))
            .thenReturn(true);

        var ok = service.placeOrder(bad);

        assertFalse(ok);
        verify(payment, times(0))
            .charge(anyString(), any());
        verify(inventory, times(0))
            .reserve(anyString(), anyInt());
    }

    @Test
    void placeOrder_throws_on_null_request() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.placeOrder(null)
        );
        verifyNoInteractions(payment, inventory, email);
    }
}
