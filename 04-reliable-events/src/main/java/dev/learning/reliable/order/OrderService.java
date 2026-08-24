package dev.learning.reliable.order;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public OrderService(OrderRepository orders, ApplicationEventPublisher events) {
        this.orders = orders;
        this.events = events;
        this.clock = Clock.systemUTC();
    }

    @Transactional
    public UUID place(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        var orderId = UUID.randomUUID();
        orders.save(new OrderEntity(orderId, customerId));
        events.publishEvent(new OrderPlaced(UUID.randomUUID(), orderId, Instant.now(clock)));
        return orderId;
    }
}

