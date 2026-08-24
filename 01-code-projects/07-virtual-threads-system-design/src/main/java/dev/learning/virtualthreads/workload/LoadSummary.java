package dev.learning.virtualthreads.workload;

public record LoadSummary(int requested, long completed, long virtualThreadExecutions,
                          int maxObservedConcurrency, long elapsedMillis) {
}

