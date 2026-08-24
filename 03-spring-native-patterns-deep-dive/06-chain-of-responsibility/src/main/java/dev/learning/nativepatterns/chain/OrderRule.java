package dev.learning.nativepatterns.chain;

import java.util.Optional;

public interface OrderRule {
    String id();

    Optional<String> validate(OrderValidationRequest request);
}
