package dev.learning.nativepatterns.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
final class VipPricingStrategy implements PricingStrategy {
    private static final BigDecimal DISCOUNT = new BigDecimal("0.90");

    @Override
    public String key() {
        return "VIP";
    }

    @Override
    public PriceQuote quote(PricingRequest request) {
        return new PriceQuote(key(), request.subtotal().multiply(DISCOUNT).setScale(2, RoundingMode.HALF_UP));
    }
}
