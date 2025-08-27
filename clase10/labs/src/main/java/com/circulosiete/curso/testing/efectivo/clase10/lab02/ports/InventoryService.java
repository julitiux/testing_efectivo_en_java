package com.circulosiete.curso.testing.efectivo.clase10.lab02.ports;

public interface InventoryService {
    boolean hasStock(String sku, int units);

    void reserve(String sku, int units);
}
