package com.habbashx.tcpserver.event;

import com.habbashx.tcpserver.cooldown.CooldownManager;
import com.habbashx.tcpserver.handler.UserHandler;

public class UserChatEvent extends Event {

    private final String username;
    private  String message;
    private final UserHandler user;

    private final CooldownManager cooldownManager;

    public UserChatEvent(String username , UserHandler user , int cooldownSecond) {
        super("UserChatEvent");
        this.username = username;
        this.user = user;
        cooldownManager = new CooldownManager(cooldownSecond);
    }

    @Override
    public void setCancelled(boolean cancelled) {
        super.setCancelled(cancelled);
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public UserHandler getUser() {
        return user;
    }

    @Override
    public boolean isCancelled() {
        return super.isCancelled();
    }

    public boolean isOnCooldown() {
        return cooldownManager.isOnCooldown(username);
    }

    public long getRemainingCooldown() {
        return cooldownManager.getRemainingTime(username);
    }

    public void applyCooldown() {
        cooldownManager.setCooldown(username);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getEventName() {
        return super.getEventName();
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof UserChatEvent chatEvent)) return false;

        return username.equals(chatEvent.username) && message.equals(chatEvent.message) && user.equals(chatEvent.user) && cooldownManager.equals(chatEvent.cooldownManager);
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + message.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + cooldownManager.hashCode();
        return result;
    }
}
