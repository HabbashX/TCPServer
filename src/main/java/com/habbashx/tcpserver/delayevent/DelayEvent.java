package com.habbashx.tcpserver.delayevent;

/**
 * Represents an abstract base class for events with a delay mechanism.
 * This class serves as a foundational data structure for creating various types of delayed events.
 * The primary purpose of this class is to encapsulate a common field and functionality
 * related to delayed events, allowing for extensibility and integration with an event-driven
 * system that supports delayed execution.
 */
public abstract class DelayEvent {

    private final String eventName;

    public DelayEvent(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}
