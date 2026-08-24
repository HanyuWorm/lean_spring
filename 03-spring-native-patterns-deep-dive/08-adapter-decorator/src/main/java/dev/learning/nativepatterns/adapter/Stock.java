package dev.learning.nativepatterns.adapter;

public record Stock(String sku, int available) {
    public Stock {
        if (available < 0) {
            throw new IllegalArgumentException("available cannot be negative");
        }
    }
}
