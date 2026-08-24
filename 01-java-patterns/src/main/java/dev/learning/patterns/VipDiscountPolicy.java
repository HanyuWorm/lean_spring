package dev.learning.patterns;

import java.math.BigDecimal;

public final class VipDiscountPolicy implements DiscountPolicy {

    private static final BigDecimal VIP_RATE = new BigDecimal("0.90");

    @Override
    public boolean supports(CustomerSegment segment) {
        return segment == CustomerSegment.VIP;
    }

    @Override
    public BigDecimal apply(BigDecimal originalPrice) {
        return originalPrice.multiply(VIP_RATE);
    }
}

