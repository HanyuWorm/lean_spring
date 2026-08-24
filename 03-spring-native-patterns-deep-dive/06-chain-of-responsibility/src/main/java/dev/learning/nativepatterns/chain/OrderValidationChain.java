package dev.learning.nativepatterns.chain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public final class OrderValidationChain {
    private final List<OrderRule> rules;

    public OrderValidationChain(List<OrderRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public ValidationResult validate(OrderValidationRequest request) {
        var visited = new ArrayList<String>();
        for (var rule : rules) {
            visited.add(rule.id());
            var failure = rule.validate(request);
            if (failure.isPresent()) {
                return ValidationResult.rejected(visited, failure.orElseThrow());
            }
        }
        return ValidationResult.accepted(visited);
    }
}
