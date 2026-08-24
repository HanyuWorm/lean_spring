package dev.learning.nativepatterns.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PricingStrategyTest {
    @Autowired
    private PricingService pricing;

    @Test
    void selectsPolicyWithoutConditionalLogicInTheUseCase() {
        assertThat(pricing.quote("STANDARD", new BigDecimal("100.00")))
                .isEqualTo(new PriceQuote("STANDARD", new BigDecimal("100.00")));
        assertThat(pricing.quote("VIP", new BigDecimal("100.00")))
                .isEqualTo(new PriceQuote("VIP", new BigDecimal("90.00")));
    }

    @Test
    void rejectsMissingAndDuplicatePolicies() {
        assertThatThrownBy(() -> pricing.quote("UNKNOWN", BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);

        var first = fixedStrategy("DUPLICATE");
        var second = fixedStrategy("DUPLICATE");
        assertThatThrownBy(() -> new PricingStrategyRegistry(List.of(first, second)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DUPLICATE");
    }

    private static PricingStrategy fixedStrategy(String key) {
        return new PricingStrategy() {
            @Override
            public String key() {
                return key;
            }

            @Override
            public PriceQuote quote(PricingRequest request) {
                return new PriceQuote(key, request.subtotal());
            }
        };
    }
}
