package io.github.hanyuworm.lld.notification;

public record Notification(String id, String recipient, String body) {
    public Notification {
        if (id == null || id.isBlank() || recipient == null || recipient.isBlank() || body == null || body.isBlank()) {
            throw new IllegalArgumentException("Notification fields are required");
        }
    }
}
