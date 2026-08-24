package dev.learning.nativepatterns.strategy;

import java.math.BigDecimal;

public record PricingRequest(String customerTier, BigDecimal subtotal) {
    public PricingRequest {
        if (customerTier == null || customerTier.isBlank()) {
            throw new IllegalArgumentException("customerTier is required");
        }
        if (subtotal == null || subtotal.signum() < 0) {
            throw new IllegalArgumentException("subtotal must be non-negative");
        }
    }
}
