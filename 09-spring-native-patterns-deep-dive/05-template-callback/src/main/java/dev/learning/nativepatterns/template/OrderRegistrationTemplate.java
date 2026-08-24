package dev.learning.nativepatterns.template;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public final class OrderRegistrationTemplate {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    public OrderRegistrationTemplate(JdbcTemplate jdbc, TransactionTemplate transactions) {
        this.jdbc = jdbc;
        this.transactions = transactions;
    }

    public void register(String orderId, AfterOrderWrite callback) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId is required");
        }
        transactions.executeWithoutResult(status -> {
            jdbc.update("insert into registered_order(order_id, status) values (?, ?)", orderId, "NEW");
            callback.execute(orderId);
        });
    }

    public int countOrders() {
        return jdbc.queryForObject("select count(*) from registered_order", Integer.class);
    }

    public void deleteAll() {
        jdbc.update("delete from registered_order");
    }
}
