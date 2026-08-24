package dev.learning.observability;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
class ThreadExecutionService {

    private final AsyncTaskExecutor executor;

    ThreadExecutionService(AsyncTaskExecutor executor) {
        this.executor = executor;
    }

    CompletableFuture<ThreadInfo> inspectExecutorThread() {
        return executor.submitCompletable(() -> {
            var thread = Thread.currentThread();
            return new ThreadInfo(thread.getName(), thread.isVirtual());
        });
    }

    record ThreadInfo(String name, boolean virtual) {
    }
}

