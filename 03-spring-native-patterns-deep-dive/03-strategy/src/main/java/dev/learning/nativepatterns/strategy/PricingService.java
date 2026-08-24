package dev.learning.nativepatterns.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public final class PricingService {
    private final PricingStrategyRegistry registry;

    public PricingService(PricingStrategyRegistry registry) {
        this.registry = registry;
    }

    public PriceQuote quote(String tier, BigDecimal subtotal) {
        var request = new PricingRequest(tier, subtotal);
        return registry.required(tier).quote(request);
    }
}
