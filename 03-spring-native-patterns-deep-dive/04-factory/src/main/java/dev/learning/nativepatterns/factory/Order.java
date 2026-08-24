package dev.learning.nativepatterns.factory;

import java.math.BigDecimal;
import java.time.Instant;

public record Order(String id, String customerId, BigDecimal total, Instant createdAt, String status) {
}
