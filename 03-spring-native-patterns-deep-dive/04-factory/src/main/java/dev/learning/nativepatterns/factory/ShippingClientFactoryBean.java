package dev.learning.nativepatterns.factory;

import org.springframework.beans.factory.FactoryBean;

public final class ShippingClientFactoryBean implements FactoryBean<ShippingClient> {
    private final ShippingClient singleton;

    public ShippingClientFactoryBean(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("endpoint is required");
        }
        this.singleton = new ConfiguredShippingClient(endpoint);
    }

    @Override
    public ShippingClient getObject() {
        return singleton;
    }

    @Override
    public Class<?> getObjectType() {
        return ShippingClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
