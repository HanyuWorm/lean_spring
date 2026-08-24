package dev.learning.nativepatterns.strategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public final class PricingStrategyRegistry {
    private final Map<String, PricingStrategy> strategies;

    public PricingStrategyRegistry(List<PricingStrategy> strategies) {
        this.strategies = strategies.stream().collect(Collectors.toUnmodifiableMap(
                PricingStrategy::key,
                Function.identity(),
                (first, duplicate) -> {
                    throw new IllegalStateException("Duplicate pricing strategy: " + first.key());
                }));
    }

    public PricingStrategy required(String key) {
        var strategy = strategies.get(key);
        if (strategy == null) {
            throw new IllegalArgumentException("No pricing strategy for tier: " + key);
        }
        return strategy;
    }
}
