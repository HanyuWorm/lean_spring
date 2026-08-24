package dev.learning.nativepatterns.chain;

import java.math.BigDecimal;

public record OrderValidationRequest(boolean customerActive, BigDecimal amount, int riskScore) {
}
