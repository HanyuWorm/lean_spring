package dev.learning.springcore.payment;

import java.math.BigDecimal;

public record PaymentCommand(String method, String reference, BigDecimal amount) {
}

