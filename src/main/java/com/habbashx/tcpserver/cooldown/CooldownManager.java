package com.habbashx.tcpserver.cooldown;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The CooldownManager class provides functionality for managing cooldown periods
 * for various users. It allows setting, checking, and removing cooldown timers
 * for user-specific operations, making it suitable for rate-limiting actions
 * or enforcing time-based restrictions.
 *
 * Constructor Summary:
 * - Default constructor initializes the CooldownManager without setting a specific
 *   cooldown time.
 * - Parameterized constructor initializes the CooldownManager with a specified cooldown
 *   time in seconds.
 *
 * Methods:
 * - isOnCooldown(String user): Checks whether the specified user is currently on cooldown.
 * - getRemainingTime(String user): Retrieves the remaining cooldown time in seconds for
 *   the specified user. Returns 0 if the user is not on cooldown.
 * - setCooldown(String user): Places the specified user on cooldown, applying the predefined
 *   cooldown time.
 * - removeCooldown(String user): Removes the cooldown for the specified user.
 * - setCooldownTime(long cooldownTime): Sets the global cooldown time in seconds.
 *
 * Thread-Safety:
 * - The class is thread-safe, as the internal map for managing user cooldowns is a
 *   ConcurrentHashMap, preventing race conditions in concurrent environments.
 *
 * Use-Cases:
 * - Enforcing time intervals between commands or operations.
 * - Implementing rate limiting for users in a multi-threaded system.
 * - Managing user-specific operational delays.
 */
public class CooldownManager {

    /**
     * A thread-safe map that stores cooldown data for users.
     * The map keys represent user identifiers, and the values represent the expiration timestamps of their cooldowns in milliseconds.
     * This map is used to manage and track the cooldown state of users.
     */
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    /**
     * Represents the cooldown duration in milliseconds. This value is used to determine the time
     * interval during which an action is restricted for a user after it has been performed.
     *
     * The cooldown time is applied consistently across all users and is converted from seconds
     * when set through the appropriate methods or constructor.
     */
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