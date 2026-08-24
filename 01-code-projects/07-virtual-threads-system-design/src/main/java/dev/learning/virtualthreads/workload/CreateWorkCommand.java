package dev.learning.virtualthreads.workload;

public record CreateWorkCommand(String customerId, long processingMillis) {

    public CreateWorkCommand {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (processingMillis < 0 || processingMillis > 10_000) {
            throw new IllegalArgumentException("processingMillis must be between 0 and 10000");
        }
    }
}

