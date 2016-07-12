package io.gatekeeper.node;

import io.gatekeeper.node.events.Event;
import io.gatekeeper.node.events.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * A class for firing and dispatching arbitrary events.
 */
public class EventManager {

    private List<EventListener> listeners = new ArrayList<>();

    /**
     * Add an event listener for the given event.
     *
     * @param event    The event class to listen to
     * @param runnable A runnable to fire when the event is fired
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void listen(Class<T> event, Function<T, Void> runnable) {
        listeners.add(new EventListener(event, runnable));
    }

    /**
     * Add an event listener for the given event.
     *
     * @param event    The event class to listen to
     * @param runnable A runnable to fire when the event is fired
     * @param context  An executor to fire the callback in
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void listen(Class<T> event, Function<T, Void> runnable, Executor context) {
        listeners.add(new EventListener(event, runnable, context));
    }

    /**
     * Fire an event
     *
     * @param event The event to fire
     */
    public void fire(Event event) {
        for (EventListener listener : listeners) {
            if (event.getClass().isAssignableFrom(listener.event)) {
                fire(event, listener);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void fire(T event, EventListener listener) {
        try {
            if (listener.executor == null) {
                listener.runnable.apply(event);
            } else {
                listener.executor.execute(() -> listener.runnable.apply(event));
            }
        } catch (Exception exception) {
            // TODO: Log!
        }
    }
}
