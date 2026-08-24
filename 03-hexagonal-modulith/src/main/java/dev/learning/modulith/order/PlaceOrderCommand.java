package dev.learning.modulith.order;

public record PlaceOrderCommand(String customerId, String sku, int quantity) {
}

