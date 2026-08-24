package dev.learning.nativepatterns.di;

public record NotificationRequest(String recipient, String message) {
    public NotificationRequest {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("recipient is required");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message is required");
        }
    }
}
