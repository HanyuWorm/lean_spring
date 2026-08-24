package dev.learning.virtualthreads.workload;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WorkloadService {

    public static final int DOWNSTREAM_CONCURRENCY_LIMIT = 4;

    private final WorkItemRepository repository;
    private final HikariDataSource dataSource;
    private final AtomicInteger inFlight = new AtomicInteger();
    private final AtomicInteger maxObservedConcurrency = new AtomicInteger();

    WorkloadService(WorkItemRepository repository, HikariDataSource dataSource) {
        this.repository = repository;
        this.dataSource = dataSource;
    }

    @Transactional
    @ConcurrencyLimit(DOWNSTREAM_CONCURRENCY_LIMIT)
    public WorkResult process(CreateWorkCommand command) {
        int current = inFlight.incrementAndGet();
        maxObservedConcurrency.accumulateAndGet(current, Math::max);
        try {
            var thread = Thread.currentThread();
            var item = new WorkItem(UUID.randomUUID(), command.customerId(),
                    thread.getName(), thread.isVirtual());

            // Force SQL execution and connection acquisition before simulated blocking I/O.
            repository.saveAndFlush(item);
            simulateBlockingIo(command.processingMillis());
            item.complete();

            return new WorkResult(item.id(), thread.getName(), thread.isVirtual());
        }
        finally {
            inFlight.decrementAndGet();
        }
    }

    private void simulateBlockingIo(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("work interrupted", exception);
        }
    }

    public WorkloadStats stats() {
        return new WorkloadStats(repository.count(), inFlight.get(), maxObservedConcurrency.get(),
                DOWNSTREAM_CONCURRENCY_LIMIT, dataSource.getMaximumPoolSize());
    }

    public int inFlight() {
        return inFlight.get();
    }

    public int maxObservedConcurrency() {
        return maxObservedConcurrency.get();
    }

    public void resetMeasurements() {
        maxObservedConcurrency.set(inFlight.get());
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}
