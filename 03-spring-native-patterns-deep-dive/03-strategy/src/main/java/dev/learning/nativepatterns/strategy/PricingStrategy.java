package dev.learning.nativepatterns.strategy;

public interface PricingStrategy {
    String key();

    PriceQuote quote(PricingRequest request);
}
