package dev.learning.resilience.provider;

import java.math.BigDecimal;

record ProviderChargeRequest(String merchantReference, BigDecimal value, String currencyCode) {
}

