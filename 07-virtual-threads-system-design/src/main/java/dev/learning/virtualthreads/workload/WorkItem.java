package dev.learning.virtualthreads.workload;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "work_items")
class WorkItem {

    @Id
    private UUID id;

    private String customerId;

    private String status;

    private String processedByThread;

    private boolean virtualThread;

    private Instant createdAt;

    @Version
    private long version;

    protected WorkItem() {
    }

    WorkItem(UUID id, String customerId, String threadName, boolean virtualThread) {
        this.id = id;
        this.customerId = customerId;
        this.status = "PROCESSING";
        this.processedByThread = threadName;
        this.virtualThread = virtualThread;
        this.createdAt = Instant.now();
    }

    void complete() {
        this.status = "COMPLETED";
    }

    UUID id() {
        return id;
    }
}
