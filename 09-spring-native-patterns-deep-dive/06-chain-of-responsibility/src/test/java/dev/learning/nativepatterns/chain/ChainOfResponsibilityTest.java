package dev.learning.nativepatterns.chain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChainOfResponsibilityTest {
    @Autowired
    private OrderValidationChain chain;

    @Test
    void executesRulesInExplicitOrder() {
        var result = chain.validate(new OrderValidationRequest(true, new BigDecimal("100"), 10));

        assertThat(result.valid()).isTrue();
        assertThat(result.visitedRules())
                .containsExactly("customer-active", "positive-amount", "risk-score");
    }

    @Test
    void shortCircuitsAfterFirstFailure() {
        var result = chain.validate(new OrderValidationRequest(false, BigDecimal.TEN, 99));

        assertThat(result.valid()).isFalse();
        assertThat(result.error()).isEqualTo("CUSTOMER_INACTIVE");
        assertThat(result.visitedRules()).containsExactly("customer-active");
    }
}
