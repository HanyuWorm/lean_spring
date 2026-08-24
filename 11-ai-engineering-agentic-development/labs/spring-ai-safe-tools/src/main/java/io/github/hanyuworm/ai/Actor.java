package io.github.hanyuworm.ai;

import java.util.Set;

public record Actor(String userId, String tenantId, Set<String> roles) {
    public Actor {
        roles = Set.copyOf(roles);
    }
}
