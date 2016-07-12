package io.gatekeeper.node.events;

import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Represents a single event listener.
 */
public class EventListener<T extends Event> {

    public Class<T> event;

    public Function<T, Void> runnable;

    public Executor executor;

    public EventListener(
        Class<T> event,
        Function<T, Void> runnable
    ) {
        this.event = event;
        this.runnable = runnable;
    }

    public EventListener(
        Class<T> event,
        Function<T, Void> runnable,
        Executor executor
    ) {
        this.event = event;
        this.runnable = runnable;
        this.executor = executor;
    }
}
