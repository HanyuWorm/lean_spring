package dev.learning.patterns;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PatternsTest {

    @Test
    void selectsDiscountStrategyWithoutSwitchStatement() {
        var registry = new DiscountPolicyRegistry(List.of(
                new StandardDiscountPolicy(),
                new VipDiscountPolicy()
        ));

        assertThat(registry.policyFor(CustomerSegment.VIP).apply(new BigDecimal("100.00")))
                .isEqualByComparingTo("90.0000");
    }

    @Test
    void executesValidationRulesAsAChain() {
        OrderRule customerRequired = order -> order.customerId() == null || order.customerId().isBlank()
                ? Optional.of("customer is required") : Optional.empty();
        OrderRule positiveQuantity = order -> order.quantity() <= 0
                ? Optional.of("quantity must be positive") : Optional.empty();
        var validator = new OrderValidator(List.of(customerRequired, positiveQuantity));

        assertThat(validator.validate(new OrderDraft("", 0, BigDecimal.TEN)))
                .containsExactly("customer is required", "quantity must be positive");
    }

    @Test
    void decoratesBaseCalculationWithTax() {
        PriceCalculator calculator = new TaxedPriceCalculator(
                new BasePriceCalculator(), new BigDecimal("0.10"));

        assertThat(calculator.total(new OrderDraft("C-1", 2, new BigDecimal("50.00"))))
                .isEqualByComparingTo("110.0000");
    }
}

