package dev.learning.nativepatterns.factory;

final class ConfiguredShippingClient implements ShippingClient {
    private final String endpoint;

    ConfiguredShippingClient(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String endpoint() {
        return endpoint;
    }

    @Override
    public String quote(String postalCode) {
        return endpoint + "/quotes/" + postalCode;
    }
}
