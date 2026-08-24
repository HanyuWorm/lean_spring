package dev.learning.nativepatterns.chain;

import java.util.List;

public record ValidationResult(boolean valid, List<String> visitedRules, String error) {
    public static ValidationResult accepted(List<String> visitedRules) {
        return new ValidationResult(true, List.copyOf(visitedRules), null);
    }

    public static ValidationResult rejected(List<String> visitedRules, String error) {
        return new ValidationResult(false, List.copyOf(visitedRules), error);
    }
}
