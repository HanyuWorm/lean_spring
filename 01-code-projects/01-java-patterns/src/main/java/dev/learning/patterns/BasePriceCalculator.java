package dev.learning.patterns;

import java.math.BigDecimal;

public final class BasePriceCalculator implements PriceCalculator {

    @Override
    public BigDecimal total(OrderDraft order) {
        return order.unitPrice().multiply(BigDecimal.valueOf(order.quantity()));
    }
}

