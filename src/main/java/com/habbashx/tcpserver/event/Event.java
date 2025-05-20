package com.habbashx.tcpserver.event;

/**
 * Represents an abstract base class for all events in the system.
 * This class defines the core properties and methods that every event must have.
 * Events are used as a mechanism to communicate and signal occurrences throughout the system.
 */
public abstract class Event {

    private final String eventName;
    private boolean isCancelled;

    public Event(String eventName) {
        this.eventName=eventName;
    }


    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public String getEventName() {
        return eventName;
    }
}
