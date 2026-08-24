package dev.learning.patterns;

import java.util.List;

public final class DiscountPolicyRegistry {

    private final List<DiscountPolicy> policies;

    public DiscountPolicyRegistry(List<DiscountPolicy> policies) {
        this.policies = List.copyOf(policies);
    }

    public DiscountPolicy policyFor(CustomerSegment segment) {
        return policies.stream()
                .filter(policy -> policy.supports(segment))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No policy for " + segment));
    }
}

