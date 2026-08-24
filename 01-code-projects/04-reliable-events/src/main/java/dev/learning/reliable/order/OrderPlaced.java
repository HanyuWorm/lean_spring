package dev.learning.reliable.order;

import java.time.Instant;
import java.util.UUID;

public record OrderPlaced(UUID eventId, UUID orderId, Instant occurredAt) {
}

