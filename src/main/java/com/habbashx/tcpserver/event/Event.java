package com.habbashx.tcpserver.event;

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
