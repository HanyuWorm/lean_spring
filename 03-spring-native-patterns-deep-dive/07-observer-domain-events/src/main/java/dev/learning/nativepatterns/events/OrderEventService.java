package dev.learning.nativepatterns.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderEventService {
    private final JdbcTemplate jdbc;
    private final ApplicationEventPublisher events;

    public OrderEventService(JdbcTemplate jdbc, ApplicationEventPublisher events) {
        this.jdbc = jdbc;
        this.events = events;
    }

    @Transactional
    public void place(String orderId) {
        persistAndPublish(orderId);
    }

    @Transactional
    public void placeThenFail(String orderId) {
        persistAndPublish(orderId);
        throw new IllegalStateException("simulated rollback");
    }

    private void persistAndPublish(String orderId) {
        jdbc.update("insert into event_order(order_id) values (?)", orderId);
        events.publishEvent(new OrderPlaced(orderId));
    }

    public int countOrders() {
        return jdbc.queryForObject("select count(*) from event_order", Integer.class);
    }

    public void deleteAll() {
        jdbc.update("delete from event_order");
    }
}
