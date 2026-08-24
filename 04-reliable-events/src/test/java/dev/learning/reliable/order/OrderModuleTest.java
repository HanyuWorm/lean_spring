package dev.learning.reliable.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.PublishedEvents;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest
class OrderModuleTest {

    @Autowired
    OrderService orderService;

    @Test
    void savesOrderAndPublishesDomainEvent(PublishedEvents events) {
        var orderId = orderService.place("C-100");

        var matchingEvents = events.ofType(OrderPlaced.class)
                .matching(OrderPlaced::orderId, orderId::equals);

        assertThat(matchingEvents).hasSize(1);
    }
}
