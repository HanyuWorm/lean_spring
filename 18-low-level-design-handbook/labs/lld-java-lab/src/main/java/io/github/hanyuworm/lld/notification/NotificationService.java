package io.github.hanyuworm.lld.notification;

import java.util.Map;

public final class NotificationService {
    private final Map<Channel, NotificationChannel> channels;

    public NotificationService(Map<Channel, NotificationChannel> channels) {
        this.channels = Map.copyOf(channels);
    }

    public NotificationChannel.DeliveryReceipt send(Channel channel, Notification notification) {
        var selected = channels.get(channel);
        if (selected == null) throw new UnsupportedChannelException(channel);
        return selected.send(notification);
    }

    public enum Channel { EMAIL, SMS, PUSH }
    public static final class UnsupportedChannelException extends RuntimeException {
        public UnsupportedChannelException(Channel channel) { super("Unsupported channel: " + channel); }
    }
}
