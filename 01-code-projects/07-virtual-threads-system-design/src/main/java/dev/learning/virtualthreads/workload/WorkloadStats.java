package dev.learning.virtualthreads.workload;

public record WorkloadStats(long persistedItems, int inFlight, int maxObservedConcurrency,
                            int configuredConcurrencyLimit, int hikariMaximumPoolSize) {
}

