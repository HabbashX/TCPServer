package com.habbashx.tcpserver.event;

import com.habbashx.tcpserver.connection.UserHandler;

/**
 * Represents an event that is triggered when a user leaves the system.
 * This event extends the base {@link Event} class and contains information
 * about the user who triggered the event and their username.
 *
 * The {@code UserLeaveEvent} is utilized to notify the system of a user's departure
 * and allows for additional processing, such as logging or broadcasting the user's disconnection.
 */
public class UserLeaveEvent extends Event {

    private final String username;
    private final UserHandler user;

    public UserLeaveEvent(String username, UserHandler user){
        super("UserLeaveEvent");
        this.username = username;
        this.user = user;
    }

    public String getUsername() {
        return username;
    }

    public UserHandler getUser() {
        return user;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        super.setCancelled(cancelled);
    }

    @Override
    public boolean isCancelled() {
        return super.isCancelled();
    }

    @Override
    public String getEventName() {
        return super.getEventName();
    }
}
