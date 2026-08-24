package dev.learning.nativepatterns.adapter;

import org.springframework.stereotype.Service;

@Service
public final class InventoryService {
    private final InventoryPort inventory;

    public InventoryService(InventoryPort inventory) {
        this.inventory = inventory;
    }

    public Stock check(String sku) {
        return inventory.getStock(sku);
    }
}
