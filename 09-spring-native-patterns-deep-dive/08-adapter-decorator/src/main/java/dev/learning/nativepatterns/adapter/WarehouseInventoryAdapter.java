package dev.learning.nativepatterns.adapter;

final class WarehouseInventoryAdapter implements InventoryPort {
    private final FakeWarehouseClient client;
    private final InventoryMetrics metrics;

    WarehouseInventoryAdapter(FakeWarehouseClient client, InventoryMetrics metrics) {
        this.client = client;
        this.metrics = metrics;
    }

    @Override
    public Stock getStock(String sku) {
        metrics.warehouseLoad();
        try {
            var external = client.fetch(sku);
            return new Stock(external.productCode(), external.onHand() - external.reserved());
        }
        catch (WarehouseSkuNotFoundException exception) {
            throw new UnknownSkuException(sku);
        }
    }
}
