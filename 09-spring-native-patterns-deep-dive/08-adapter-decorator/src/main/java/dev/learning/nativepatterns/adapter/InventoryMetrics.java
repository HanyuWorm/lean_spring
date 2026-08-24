package dev.learning.nativepatterns.adapter;

import java.util.concurrent.atomic.AtomicInteger;

public final class InventoryMetrics {
    private final AtomicInteger logicalCalls = new AtomicInteger();
    private final AtomicInteger warehouseLoads = new AtomicInteger();

    void logicalCall() {
        logicalCalls.incrementAndGet();
    }

    void warehouseLoad() {
        warehouseLoads.incrementAndGet();
    }

    public int logicalCalls() {
        return logicalCalls.get();
    }

    public int warehouseLoads() {
        return warehouseLoads.get();
    }

    public void reset() {
        logicalCalls.set(0);
        warehouseLoads.set(0);
    }
}
