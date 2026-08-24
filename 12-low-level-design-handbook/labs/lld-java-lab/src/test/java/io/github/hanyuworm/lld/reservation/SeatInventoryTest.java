package io.github.hanyuworm.lld.reservation;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SeatInventoryTest {
    private static final Instant NOW = Instant.parse("2026-08-25T00:00:00Z");

    @Test
    void sameIdempotencyKeyReturnsTheOriginalHold() {
        var inventory = new SeatInventory();
        var first = assertInstanceOf(SeatInventory.Held.class,
                inventory.hold("A-1", "U-1", "K-1", 0, NOW, Duration.ofMinutes(5)));
        var replay = assertInstanceOf(SeatInventory.Held.class,
                inventory.hold("A-1", "U-1", "K-1", 0, NOW.plusSeconds(1), Duration.ofMinutes(5)));
        assertEquals(first.hold(), replay.hold());
        assertEquals(1, inventory.versionOf("A-1"));
    }

    @Test
    void sameIdempotencyKeyWithDifferentPayloadIsAConflict() {
        var inventory = new SeatInventory();
        inventory.hold("A-1", "U-1", "K-1", 0, NOW, Duration.ofMinutes(5));
        var conflict = inventory.hold("A-1", "U-1", "K-1", 0, NOW, Duration.ofMinutes(10));
        assertInstanceOf(SeatInventory.IdempotencyConflict.class, conflict);
    }

    @Test
    void exactlyOneConcurrentOwnerWinsTheSeat() throws Exception {
        var inventory = new SeatInventory();
        int contenders = 20;
        var ready = new CountDownLatch(contenders);
        var start = new CountDownLatch(1);
        var results = new ArrayList<SeatInventory.HoldResult>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < contenders; i++) {
                int owner = i;
                executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    var result = inventory.hold("A-1", "U-" + owner, "K-" + owner,
                            0, NOW, Duration.ofMinutes(5));
                    synchronized (results) { results.add(result); }
                    return null;
                });
            }
            ready.await();
            start.countDown();
        }
        assertEquals(1, results.stream().filter(SeatInventory.Held.class::isInstance).count());
        assertEquals(contenders - 1, results.stream().filter(SeatInventory.Unavailable.class::isInstance).count());
    }

    @Test
    void expiredHoldRequiresTheLatestVersion() {
        var inventory = new SeatInventory();
        inventory.hold("A-1", "U-1", "K-1", 0, NOW, Duration.ofSeconds(1));
        var stale = inventory.hold("A-1", "U-2", "K-2", 0, NOW.plusSeconds(1), Duration.ofMinutes(5));
        assertInstanceOf(SeatInventory.VersionConflict.class, stale);
    }
}
