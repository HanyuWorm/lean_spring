package dev.learning.resilience.provider;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(accept = "application/json", contentType = "application/json")
interface PaymentProviderApi {

    @PostExchange("/v1/charges")
    ProviderChargeResponse charge(@RequestBody ProviderChargeRequest request);
}

