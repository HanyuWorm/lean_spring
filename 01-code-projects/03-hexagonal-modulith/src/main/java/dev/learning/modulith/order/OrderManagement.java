package dev.learning.modulith.order;

import dev.learning.modulith.order.domain.Order;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderManagement {

    private final OrderStore store;
    private final ApplicationEventPublisher events;

    public OrderManagement(OrderStore store, ApplicationEventPublisher events) {
        this.store = store;
        this.events = events;
    }

    public UUID place(PlaceOrderCommand command) {
        var order = Order.place(command.customerId(), command.sku(), command.quantity());
        store.save(order);
        events.publishEvent(new OrderPlaced(order.id(), order.sku(), order.quantity()));
        return order.id();
    }
}

