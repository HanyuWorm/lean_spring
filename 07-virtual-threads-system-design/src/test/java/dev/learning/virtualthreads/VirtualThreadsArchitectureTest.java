package dev.learning.virtualthreads;

import dev.learning.virtualthreads.workload.LoadGenerator;
import dev.learning.virtualthreads.workload.WorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class VirtualThreadsArchitectureTest {

    @Autowired
    LoadGenerator loadGenerator;

    @Autowired
    WorkloadService workload;

    @BeforeEach
    void reset() {
        workload.deleteAll();
        workload.resetMeasurements();
    }

    @Test
    void virtualThreadsDoNotBypassFiniteDatabaseCapacity() {
        var summary = loadGenerator.run(12, 25);

        assertThat(summary.completed()).isEqualTo(12);
        assertThat(summary.virtualThreadExecutions()).isEqualTo(12);
        assertThat(summary.maxObservedConcurrency())
                .isBetween(1, WorkloadService.DOWNSTREAM_CONCURRENCY_LIMIT);
        assertThat(workload.stats().persistedItems()).isEqualTo(12);
    }
}

