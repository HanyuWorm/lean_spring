package dev.learning.modulith.order;

import java.util.UUID;

public record OrderPlaced(UUID orderId, String sku, int quantity) {
}

