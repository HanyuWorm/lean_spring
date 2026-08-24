package dev.learning.resilience.provider;

public final class PaymentProviderException extends RuntimeException {

    public PaymentProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}

