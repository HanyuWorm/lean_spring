package dev.learning.nativepatterns.chain;

import java.util.Optional;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
final class CustomerActiveRule implements OrderRule {
    @Override
    public String id() {
        return "customer-active";
    }

    @Override
    public Optional<String> validate(OrderValidationRequest request) {
        return request.customerActive() ? Optional.empty() : Optional.of("CUSTOMER_INACTIVE");
    }
}
