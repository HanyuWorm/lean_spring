package dev.learning.nativepatterns.adapter;

final class CountingInventoryDecorator implements InventoryPort {
    private final InventoryPort delegate;
    private final InventoryMetrics metrics;

    CountingInventoryDecorator(InventoryPort delegate, InventoryMetrics metrics) {
        this.delegate = delegate;
        this.metrics = metrics;
    }

    @Override
    public Stock getStock(String sku) {
        metrics.logicalCall();
        return delegate.getStock(sku);
    }
}
