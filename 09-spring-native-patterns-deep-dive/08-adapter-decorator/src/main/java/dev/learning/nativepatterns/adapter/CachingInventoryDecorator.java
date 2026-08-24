package dev.learning.nativepatterns.adapter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class CachingInventoryDecorator implements InventoryPort {
    private final InventoryPort delegate;
    private final ConcurrentMap<String, Stock> cache = new ConcurrentHashMap<>();

    CachingInventoryDecorator(InventoryPort delegate) {
        this.delegate = delegate;
    }

    @Override
    public Stock getStock(String sku) {
        return cache.computeIfAbsent(sku, delegate::getStock);
    }
}
