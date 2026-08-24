package dev.learning.resilience.payment;

import java.math.BigDecimal;

public record ChargePayment(String orderId, BigDecimal amount, String currency) {
}

