package io.github.hanyuworm.ai;

public final class ToolPolicy {
    public void authorizeOrderRead(Actor actor) {
        if (actor == null || actor.tenantId() == null || !actor.roles().contains("ORDER_READ")) {
            throw new ToolAccessDeniedException();
        }
    }

    public static final class ToolAccessDeniedException extends RuntimeException {
        public ToolAccessDeniedException() {
            super("Tool access denied");
        }
    }
}
