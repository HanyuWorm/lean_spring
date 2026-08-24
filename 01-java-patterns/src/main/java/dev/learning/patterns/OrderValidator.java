package dev.learning.patterns;

import java.util.List;

public final class OrderValidator {

    private final List<OrderRule> rules;

    public OrderValidator(List<OrderRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public List<String> validate(OrderDraft order) {
        return rules.stream()
                .map(rule -> rule.violationFor(order))
                .flatMap(java.util.Optional::stream)
                .toList();
    }
}

