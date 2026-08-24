package dev.learning.nativepatterns.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ObserverDomainEventsTest {
    @Autowired
    private OrderEventService orders;

    @Autowired
    private ImmediateOrderListener immediate;

    @Autowired
    private CommittedOrderListener committed;

    @BeforeEach
    void reset() {
        orders.deleteAll();
        immediate.clear();
        committed.clear();
    }

    @Test
    void bothObserversSeeAnEventAfterSuccessfulCommit() {
        orders.place("order-1");

        assertThat(orders.countOrders()).isOne();
        assertThat(immediate.observedOrderIds()).containsExactly("order-1");
        assertThat(committed.observedOrderIds()).containsExactly("order-1");
    }

    @Test
    void ordinaryListenerRunsBeforeRollbackButTransactionalListenerDoesNot() {
        assertThatThrownBy(() -> orders.placeThenFail("order-rollback"))
                .isInstanceOf(IllegalStateException.class);

        assertThat(orders.countOrders()).isZero();
        assertThat(immediate.observedOrderIds()).containsExactly("order-rollback");
        assertThat(committed.observedOrderIds()).isEmpty();
    }
}
