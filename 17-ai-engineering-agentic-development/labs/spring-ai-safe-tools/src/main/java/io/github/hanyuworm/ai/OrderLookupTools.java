package io.github.hanyuworm.ai;

import org.springframework.ai.tool.annotation.Tool;

import java.util.Objects;
import java.util.regex.Pattern;

public final class OrderLookupTools {
    private static final Pattern ORDER_ID = Pattern.compile("ORD-[0-9]{1,12}");

    private final ActorProvider actorProvider;
    private final ToolPolicy policy;
    private final OrderService orderService;

    public OrderLookupTools(ActorProvider actorProvider, ToolPolicy policy, OrderService orderService) {
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.policy = Objects.requireNonNull(policy);
        this.orderService = Objects.requireNonNull(orderService);
    }

    @Tool(description = "Tra cứu trạng thái một đơn hàng theo ID; chỉ đọc và chỉ trong tenant của người dùng hiện tại")
    public OrderView findOrder(String orderId) {
        if (orderId == null || !ORDER_ID.matcher(orderId).matches()) {
            throw new IllegalArgumentException("Invalid order ID");
        }
        var actor = actorProvider.currentActor();
        policy.authorizeOrderRead(actor);
        return orderService.findForTenant(actor.tenantId(), orderId);
    }
}
