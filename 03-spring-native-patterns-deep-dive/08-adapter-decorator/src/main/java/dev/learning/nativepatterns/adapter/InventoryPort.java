package dev.learning.nativepatterns.adapter;

public interface InventoryPort {
    Stock getStock(String sku);
}
