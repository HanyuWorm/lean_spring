package dev.learning.nativepatterns.adapter;

import java.util.Map;

public final class FakeWarehouseClient {
    private final Map<String, WarehouseResponse> responses = Map.of(
            "SKU-1", new WarehouseResponse("SKU-1", 12, 2),
            "SKU-2", new WarehouseResponse("SKU-2", 5, 5));

    WarehouseResponse fetch(String sku) {
        var response = responses.get(sku);
        if (response == null) {
            throw new WarehouseSkuNotFoundException(sku);
        }
        return response;
    }
}
