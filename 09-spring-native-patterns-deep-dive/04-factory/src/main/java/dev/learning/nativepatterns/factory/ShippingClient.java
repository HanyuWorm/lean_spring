package dev.learning.nativepatterns.factory;

public interface ShippingClient {
    String endpoint();

    String quote(String postalCode);
}
