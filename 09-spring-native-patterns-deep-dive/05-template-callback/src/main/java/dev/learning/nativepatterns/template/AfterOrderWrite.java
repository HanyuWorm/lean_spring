package dev.learning.nativepatterns.template;

@FunctionalInterface
public interface AfterOrderWrite {
    void execute(String orderId);
}
