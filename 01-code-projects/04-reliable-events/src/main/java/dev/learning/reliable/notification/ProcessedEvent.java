package dev.learning.reliable.notification;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
class ProcessedEvent {

    @Id
    private UUID eventId;

    private Instant processedAt;

    protected ProcessedEvent() {
    }

    ProcessedEvent(UUID eventId, Instant processedAt) {
        this.eventId = eventId;
        this.processedAt = processedAt;
    }
}

