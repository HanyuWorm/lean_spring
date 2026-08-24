package io.github.hanyuworm.ai;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderLookupToolsTest {
    private final OrderService service = new OrderService(Map.of(
            "ORD-42", new OrderService.StoredOrder("tenant-a", "PAID"),
            "ORD-99", new OrderService.StoredOrder("tenant-b", "SHIPPED")
    ));

    @Test
    void returnsAnOrderOwnedByCurrentTenant() {
        var tools = toolsFor(new Actor("u-1", "tenant-a", Set.of("ORDER_READ")));
        assertEquals(new OrderView("ORD-42", "PAID"), tools.findOrder("ORD-42"));
    }

    @Test
    void hidesAnOrderFromAnotherTenant() {
        var tools = toolsFor(new Actor("u-1", "tenant-a", Set.of("ORDER_READ")));
        assertThrows(OrderService.OrderNotFoundException.class, () -> tools.findOrder("ORD-99"));
    }

    @Test
    void deniesActorWithoutRole() {
        var tools = toolsFor(new Actor("u-1", "tenant-a", Set.of()));
        assertThrows(ToolPolicy.ToolAccessDeniedException.class, () -> tools.findOrder("ORD-42"));
    }

    @Test
    void rejectsInvalidModelArgumentsBeforeServiceCall() {
        var tools = toolsFor(new Actor("u-1", "tenant-a", Set.of("ORDER_READ")));
        assertThrows(IllegalArgumentException.class, () -> tools.findOrder("../../secret"));
    }

    private OrderLookupTools toolsFor(Actor actor) {
        return new OrderLookupTools(() -> actor, new ToolPolicy(), service);
    }
}
