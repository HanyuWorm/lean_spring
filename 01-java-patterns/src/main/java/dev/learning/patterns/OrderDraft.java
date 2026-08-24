package dev.learning.patterns;

import java.math.BigDecimal;

public record OrderDraft(String customerId, int quantity, BigDecimal unitPrice) {
}

