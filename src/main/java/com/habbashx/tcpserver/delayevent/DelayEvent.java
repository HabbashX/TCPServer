package com.habbashx.tcpserver.delayevent;

public abstract class DelayEvent {

    private final String eventName;

    public DelayEvent(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}
