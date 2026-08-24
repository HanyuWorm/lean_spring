package dev.learning.patterns;

import java.math.BigDecimal;

public final class StandardDiscountPolicy implements DiscountPolicy {

    @Override
    public boolean supports(CustomerSegment segment) {
        return segment == CustomerSegment.STANDARD;
    }

    @Override
    public BigDecimal apply(BigDecimal originalPrice) {
        return originalPrice;
    }
}

