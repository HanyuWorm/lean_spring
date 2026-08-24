package dev.learning.nativepatterns.chain;

import java.util.Optional;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(30)
final class RiskScoreRule implements OrderRule {
    @Override
    public String id() {
        return "risk-score";
    }

    @Override
    public Optional<String> validate(OrderValidationRequest request) {
        return request.riskScore() <= 70 ? Optional.empty() : Optional.of("RISK_TOO_HIGH");
    }
}
