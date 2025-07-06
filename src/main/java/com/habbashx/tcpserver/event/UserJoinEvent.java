package com.habbashx.tcpserver.event;

import com.habbashx.tcpserver.connection.UserHandler;

/**
 * Represents an event that is triggered when a user joins the system or server.
 * This event carries information about the user who is joining, such as their username
 * and corresponding user handler. It also supports cancellation, allowing handlers to
 * prevent the user from successfully joining.
 *
 * This class extends the base {@code Event} class and inherits its functionality while
 * adding additional properties specific to user join events.
 */
public class UserJoinEvent extends Event {

    private final String username;
    private final UserHandler user;

    public UserJoinEvent(String username , UserHandler user) {
        super("UserJoinEvent");
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
