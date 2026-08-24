package dev.learning.nativepatterns.strategy;

import org.springframework.stereotype.Component;

@Component
final class StandardPricingStrategy implements PricingStrategy {
    @Override
    public String key() {
        return "STANDARD";
    }

    @Override
    public PriceQuote quote(PricingRequest request) {
        return new PriceQuote(key(), request.subtotal());
    }
}
