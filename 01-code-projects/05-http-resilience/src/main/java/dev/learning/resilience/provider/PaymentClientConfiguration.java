package dev.learning.resilience.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration(proxyBeanMethods = false)
class PaymentClientConfiguration {

    @Bean
    PaymentProviderApi paymentProviderApi(
            RestClient.Builder builder,
            @Value("${payment.provider.base-url}") String baseUrl) {
        var restClient = builder.baseUrl(baseUrl).build();
        var factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(PaymentProviderApi.class);
    }
}

