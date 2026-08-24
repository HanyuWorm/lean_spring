package dev.learning.nativepatterns.adapter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class InventoryConfiguration {
    @Bean
    FakeWarehouseClient warehouseClient() {
        return new FakeWarehouseClient();
    }

    @Bean
    InventoryMetrics inventoryMetrics() {
        return new InventoryMetrics();
    }

    @Bean
    InventoryPort inventoryPort(FakeWarehouseClient client, InventoryMetrics metrics) {
        InventoryPort adapter = new WarehouseInventoryAdapter(client, metrics);
        InventoryPort cached = new CachingInventoryDecorator(adapter);
        return new CountingInventoryDecorator(cached, metrics);
    }
}
