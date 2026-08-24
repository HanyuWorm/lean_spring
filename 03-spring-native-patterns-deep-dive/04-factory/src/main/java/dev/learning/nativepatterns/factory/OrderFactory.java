package dev.learning.nativepatterns.factory;

import java.math.BigDecimal;
import java.time.Clock;

import org.springframework.stereotype.Component;

@Component
public final class OrderFactory {
    private final OrderNumberGenerator numbers;
    private final Clock clock;

    public OrderFactory(OrderNumberGenerator numbers, Clock clock) {
        this.numbers = numbers;
        this.clock = clock;
    }

    public Order create(String customerId, BigDecimal total) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (total == null || total.signum() <= 0) {
            throw new IllegalArgumentException("total must be positive");
        }
        return new Order(numbers.next(), customerId, total, clock.instant(), "PENDING");
    }
}
