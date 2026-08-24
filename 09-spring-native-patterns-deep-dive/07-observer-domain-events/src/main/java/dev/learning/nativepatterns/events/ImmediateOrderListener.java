package dev.learning.nativepatterns.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public final class ImmediateOrderListener {
    private final List<String> observed = new CopyOnWriteArrayList<>();

    @EventListener
    void on(OrderPlaced event) {
        observed.add(event.orderId());
    }

    public List<String> observedOrderIds() {
        return List.copyOf(observed);
    }

    public void clear() {
        observed.clear();
    }
}
