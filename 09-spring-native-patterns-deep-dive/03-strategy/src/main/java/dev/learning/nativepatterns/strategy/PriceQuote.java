package dev.learning.nativepatterns.strategy;

import java.math.BigDecimal;

public record PriceQuote(String policy, BigDecimal finalPrice) {
}
