package dev.learning.virtualthreads.workload;

import java.util.UUID;

public record WorkResult(UUID id, String threadName, boolean virtualThread) {
}

