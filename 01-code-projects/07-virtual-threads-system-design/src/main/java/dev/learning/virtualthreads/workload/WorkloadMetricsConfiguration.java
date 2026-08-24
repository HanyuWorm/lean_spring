package dev.learning.virtualthreads.workload;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class WorkloadMetricsConfiguration {

    WorkloadMetricsConfiguration(MeterRegistry registry, WorkloadService workload) {
        Gauge.builder("workload.inflight", workload, WorkloadService::inFlight)
                .description("Transactions currently admitted by the application bulkhead")
                .register(registry);
        Gauge.builder("workload.max.observed.concurrency", workload,
                        WorkloadService::maxObservedConcurrency)
                .description("Maximum workload concurrency observed since reset")
                .register(registry);
    }
}

