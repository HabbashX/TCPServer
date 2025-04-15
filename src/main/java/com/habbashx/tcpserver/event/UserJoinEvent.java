package com.habbashx.tcpserver.event;

import com.habbashx.tcpserver.handler.UserHandler;

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
