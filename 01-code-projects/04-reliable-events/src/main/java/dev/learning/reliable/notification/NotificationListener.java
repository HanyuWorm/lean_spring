package dev.learning.reliable.notification;

import dev.learning.reliable.order.OrderPlaced;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
class NotificationListener {

    private final ProcessedEventRepository processedEvents;

    NotificationListener(ProcessedEventRepository processedEvents) {
        this.processedEvents = processedEvents;
    }

    @ApplicationModuleListener
    void on(OrderPlaced event) {
        if (processedEvents.existsById(event.eventId())) {
            return;
        }
        // Send notification here. A real external side effect needs its own idempotency contract.
        processedEvents.save(new ProcessedEvent(event.eventId(), Instant.now()));
    }
}

