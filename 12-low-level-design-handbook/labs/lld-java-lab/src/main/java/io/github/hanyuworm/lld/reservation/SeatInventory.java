package io.github.hanyuworm.lld.reservation;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public final class SeatInventory {
    private final ConcurrentHashMap<String, Hold> holds = new ConcurrentHashMap<>();

    public HoldResult hold(String seatId, String ownerId, String idempotencyKey,
                           long expectedVersion, Instant now, Duration ttl) {
        requireText(seatId);
        requireText(ownerId);
        requireText(idempotencyKey);
        if (expectedVersion < 0 || ttl.isNegative() || ttl.isZero()) throw new IllegalArgumentException();

        var outcome = new AtomicReference<HoldResult>();
        holds.compute(seatId, (ignored, current) -> {
            if (current != null && current.isActiveAt(now)) {
                if (current.ownerId().equals(ownerId) && current.idempotencyKey().equals(idempotencyKey)) {
                    outcome.set(current.ttl().equals(ttl)
                            ? new Held(current)
                            : new IdempotencyConflict(idempotencyKey));
                } else {
                    outcome.set(new Unavailable(seatId));
                }
                return current;
            }
            long currentVersion = current == null ? 0 : current.version();
            if (currentVersion != expectedVersion) {
                outcome.set(new VersionConflict(expectedVersion, currentVersion));
                return current;
            }
            var created = new Hold(seatId, ownerId, idempotencyKey, now.plus(ttl), ttl, currentVersion + 1);
            outcome.set(new Held(created));
            return created;
        });
        return outcome.get();
    }

    public long versionOf(String seatId) {
        var current = holds.get(seatId);
        return current == null ? 0 : current.version();
    }

    private static void requireText(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException();
    }

    public record Hold(String seatId, String ownerId, String idempotencyKey,
                       Instant expiresAt, Duration ttl, long version) {
        public Hold {
            Objects.requireNonNull(expiresAt);
            Objects.requireNonNull(ttl);
        }
        public boolean isActiveAt(Instant now) { return now.isBefore(expiresAt); }
    }

    public sealed interface HoldResult permits Held, Unavailable, VersionConflict, IdempotencyConflict {}
    public record Held(Hold hold) implements HoldResult {}
    public record Unavailable(String seatId) implements HoldResult {}
    public record VersionConflict(long expected, long actual) implements HoldResult {}
    public record IdempotencyConflict(String key) implements HoldResult {}
}
