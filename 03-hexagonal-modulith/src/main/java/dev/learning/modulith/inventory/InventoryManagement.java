package dev.learning.modulith.inventory;

import dev.learning.modulith.order.OrderPlaced;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InventoryManagement {

    private final Map<UUID, Integer> reservations = new ConcurrentHashMap<>();

    @ApplicationModuleListener
    void on(OrderPlaced event) {
        reservations.putIfAbsent(event.orderId(), event.quantity());
    }

    public boolean isReserved(UUID orderId) {
        return reservations.containsKey(orderId);
    }
}

