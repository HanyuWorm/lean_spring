package dev.learning.patterns;

import java.math.BigDecimal;

public final class TaxedPriceCalculator implements PriceCalculator {

    private final PriceCalculator delegate;
    private final BigDecimal taxRate;

    public TaxedPriceCalculator(PriceCalculator delegate, BigDecimal taxRate) {
        this.delegate = delegate;
        this.taxRate = taxRate;
    }

    @Override
    public BigDecimal total(OrderDraft order) {
        return delegate.total(order).multiply(BigDecimal.ONE.add(taxRate));
    }
}

