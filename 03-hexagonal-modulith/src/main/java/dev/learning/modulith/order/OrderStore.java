package dev.learning.modulith.order;

import dev.learning.modulith.order.domain.Order;

import java.util.Optional;
import java.util.UUID;

public interface OrderStore {

    void save(Order order);

    Optional<Order> findById(UUID id);
}

