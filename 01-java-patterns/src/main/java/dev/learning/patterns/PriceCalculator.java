package dev.learning.patterns;

import java.math.BigDecimal;

@FunctionalInterface
public interface PriceCalculator {

    BigDecimal total(OrderDraft order);
}

