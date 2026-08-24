package dev.learning.virtualthreads.workload;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Component
public class LoadGenerator {

    private final AsyncTaskExecutor executor;
    private final WorkloadService workload;

    LoadGenerator(AsyncTaskExecutor executor, WorkloadService workload) {
        this.executor = executor;
        this.workload = workload;
    }

    public LoadSummary run(int requests, long processingMillis) {
        if (requests < 1 || requests > 1_000) {
            throw new IllegalArgumentException("requests must be between 1 and 1000");
        }

        workload.resetMeasurements();
        var startedAt = Instant.now();
        List<CompletableFuture<WorkResult>> tasks = IntStream.range(0, requests)
                .mapToObj(index -> executor.submitCompletable(() -> workload.process(
                        new CreateWorkCommand("LOAD-" + index, processingMillis))))
                .toList();

        CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).join();
        var results = tasks.stream().map(CompletableFuture::join).toList();

        return new LoadSummary(
                requests,
                results.size(),
                results.stream().filter(WorkResult::virtualThread).count(),
                workload.maxObservedConcurrency(),
                Duration.between(startedAt, Instant.now()).toMillis()
        );
    }
}

