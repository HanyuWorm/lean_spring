package dev.learning.patterns;

import java.util.Optional;

@FunctionalInterface
public interface OrderRule {

    Optional<String> violationFor(OrderDraft order);
}

