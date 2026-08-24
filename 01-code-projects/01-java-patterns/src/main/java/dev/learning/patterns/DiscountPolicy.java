package dev.learning.patterns;

import java.math.BigDecimal;

public interface DiscountPolicy {

    boolean supports(CustomerSegment segment);

    BigDecimal apply(BigDecimal originalPrice);
}

