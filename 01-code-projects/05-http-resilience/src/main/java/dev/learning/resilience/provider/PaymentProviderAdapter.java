package dev.learning.resilience.provider;

import dev.learning.resilience.payment.ChargePayment;
import dev.learning.resilience.payment.PaymentPort;
import dev.learning.resilience.payment.PaymentReceipt;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class PaymentProviderAdapter implements PaymentPort {

    private final PaymentProviderApi api;

    PaymentProviderAdapter(PaymentProviderApi api) {
        this.api = api;
    }

    @Override
    @Retryable(includes = PaymentProviderException.class,
            maxRetries = 2, delay = 50, jitter = 20, multiplier = 2)
    @ConcurrencyLimit(10)
    public PaymentReceipt charge(ChargePayment command) {
        try {
            var response = api.charge(new ProviderChargeRequest(
                    command.orderId(), command.amount(), command.currency()));
            return new PaymentReceipt(response.providerTransactionId(), response.providerStatus());
        }
        catch (RestClientException exception) {
            throw new PaymentProviderException("Transient payment provider failure", exception);
        }
    }
}
