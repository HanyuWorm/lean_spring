package dev.learning.nativepatterns.chain;

import java.util.Optional;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
final class PositiveAmountRule implements OrderRule {
    @Override
    public String id() {
        return "positive-amount";
    }

    @Override
    public Optional<String> validate(OrderValidationRequest request) {
        return request.amount() != null && request.amount().signum() > 0
                ? Optional.empty()
                : Optional.of("AMOUNT_NOT_POSITIVE");
    }
}
