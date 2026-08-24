package io.github.hanyuworm.ai;

import java.util.Map;

public final class OrderService {
    private final Map<String, StoredOrder> orders;

    public OrderService(Map<String, StoredOrder> orders) {
        this.orders = Map.copyOf(orders);
    }

    public OrderView findForTenant(String tenantId, String orderId) {
        var order = orders.get(orderId);
        if (order == null || !order.tenantId().equals(tenantId)) {
            throw new OrderNotFoundException();
        }
        return new OrderView(orderId, order.status());
    }

    public record StoredOrder(String tenantId, String status) {
    }

    public static final class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException() {
            super("Order was not found");
        }
    }
}
