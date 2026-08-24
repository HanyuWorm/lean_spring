package dev.learning.modulith.order;

import dev.learning.modulith.order.domain.Order;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderDomainTest {

    @Test
    void protectsQuantityInvariantWithoutSpringContext() {
        assertThatThrownBy(() -> Order.place("C-1", "SKU-1", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity");
    }
}

