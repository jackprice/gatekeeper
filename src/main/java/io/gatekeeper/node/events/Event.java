package io.gatekeeper.node.events;

/**
 * An arbitrary event to be fired.
 */
public class Event {

    /**
     * The name of this event
     */
    protected String name;

    public Event(String name) {
        this.name = name;
    }
}
