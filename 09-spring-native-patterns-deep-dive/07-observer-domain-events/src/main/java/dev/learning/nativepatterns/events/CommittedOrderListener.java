package dev.learning.nativepatterns.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public final class CommittedOrderListener {
    private final List<String> observed = new CopyOnWriteArrayList<>();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(OrderPlaced event) {
        observed.add(event.orderId());
    }

    public List<String> observedOrderIds() {
        return List.copyOf(observed);
    }

    public void clear() {
        observed.clear();
    }
}
