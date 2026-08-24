package dev.learning.nativepatterns.adapter;

final class WarehouseSkuNotFoundException extends RuntimeException {
    WarehouseSkuNotFoundException(String sku) {
        super(sku);
    }
}
