package dev.learning.nativepatterns.adapter;

public final class UnknownSkuException extends RuntimeException {
    public UnknownSkuException(String sku) {
        super("Unknown SKU: " + sku);
    }
}
