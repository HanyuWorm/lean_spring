package dev.learning.modulith.order.domain;

import java.util.Objects;
import java.util.UUID;

public final class Order {

    private final UUID id;
    private final String customerId;
    private final String sku;
    private final int quantity;

    private Order(UUID id, String customerId, String sku, int quantity) {
        this.id = Objects.requireNonNull(id);
        this.customerId = requireText(customerId, "customerId");
        this.sku = requireText(sku, "sku");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.quantity = quantity;
    }

    public static Order place(String customerId, String sku, int quantity) {
        return new Order(UUID.randomUUID(), customerId, sku, quantity);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    public UUID id() {
        return id;
    }

    public String sku() {
        return sku;
    }

    public int quantity() {
        return quantity;
    }
}

