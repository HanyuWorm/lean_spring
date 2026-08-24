package dev.learning.nativepatterns.di;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public final class NotificationChannelRegistry {
    private final Map<String, NotificationChannel> channels;

    public NotificationChannelRegistry(List<NotificationChannel> channels) {
        this.channels = channels.stream().collect(Collectors.toUnmodifiableMap(
                NotificationChannel::key,
                Function.identity(),
                (first, duplicate) -> {
                    throw new IllegalStateException("Duplicate channel key: " + first.key());
                }));
    }

    public NotificationChannel required(String key) {
        var channel = channels.get(key);
        if (channel == null) {
            throw new IllegalArgumentException("Unsupported notification channel: " + key);
        }
        return channel;
    }

    public Set<String> supportedChannels() {
        return channels.keySet();
    }
}
