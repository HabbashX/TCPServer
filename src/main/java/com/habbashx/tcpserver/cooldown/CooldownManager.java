package com.habbashx.tcpserver.cooldown;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    private long cooldownTime;

    public CooldownManager() {}

    public CooldownManager(long cooldownTime) {
        this.cooldownTime = cooldownTime * 1000;
    }

    public boolean isOnCooldown(String user) {
        return cooldowns.containsKey(user) && System.currentTimeMillis() < cooldowns.get(user);
    }

    public long getRemainingTime(String user) {
        return isOnCooldown(user) ? (cooldowns.get(user) - System.currentTimeMillis()) / 1000 : 0;
    }

    public void setCooldown(String user) {
        cooldowns.put(user, System.currentTimeMillis() + cooldownTime);
    }

    public void removeCooldown(String user) {
        cooldowns.remove(user);
    }

    public void setCooldownTime(long cooldownTime) {
        this.cooldownTime = cooldownTime * 1000;
    }
}