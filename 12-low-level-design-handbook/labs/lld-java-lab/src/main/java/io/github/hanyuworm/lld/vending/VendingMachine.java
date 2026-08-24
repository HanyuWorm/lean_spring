package io.github.hanyuworm.lld.vending;

import java.util.HashMap;
import java.util.Map;

public final class VendingMachine {
    private final Map<String, Slot> slots = new HashMap<>();
    private State state = State.IDLE;
    private int creditCents;

    public void refill(String code, int priceCents, int quantity) {
        if (priceCents <= 0 || quantity < 0) throw new IllegalArgumentException();
        slots.put(code, new Slot(priceCents, quantity));
    }

    public void insert(int cents) {
        if (cents <= 0 || state == State.OUT_OF_SERVICE) throw new IllegalStateException();
        creditCents = Math.addExact(creditCents, cents);
        state = State.HAS_CREDIT;
    }

    public PurchaseResult select(String code) {
        if (state != State.HAS_CREDIT) return new Rejected("CREDIT_REQUIRED");
        var slot = slots.get(code);
        if (slot == null || slot.quantity == 0) return new Rejected("OUT_OF_STOCK");
        if (creditCents < slot.priceCents) return new Rejected("INSUFFICIENT_CREDIT");
        slot.quantity--;
        creditCents -= slot.priceCents;
        int change = creditCents;
        creditCents = 0;
        state = State.IDLE;
        return new Dispensed(code, change);
    }

    public int cancel() {
        int refund = creditCents;
        creditCents = 0;
        if (state != State.OUT_OF_SERVICE) state = State.IDLE;
        return refund;
    }

    public State state() { return state; }
    public int stockOf(String code) { return slots.getOrDefault(code, new Slot(1, 0)).quantity; }

    public enum State { IDLE, HAS_CREDIT, OUT_OF_SERVICE }
    public sealed interface PurchaseResult permits Dispensed, Rejected {}
    public record Dispensed(String productCode, int changeCents) implements PurchaseResult {}
    public record Rejected(String reason) implements PurchaseResult {}

    private static final class Slot {
        private final int priceCents;
        private int quantity;
        private Slot(int priceCents, int quantity) { this.priceCents = priceCents; this.quantity = quantity; }
    }
}
