package dev.learning.reliable.order;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "orders")
class OrderEntity {

    @Id
    private UUID id;

    private String customerId;

    protected OrderEntity() {
    }

    OrderEntity(UUID id, String customerId) {
        this.id = id;
        this.customerId = customerId;
    }

    UUID id() {
        return id;
    }
}

