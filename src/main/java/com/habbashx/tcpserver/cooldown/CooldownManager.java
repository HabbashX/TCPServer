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
public final class CooldownManager {

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

    /**
     * Checks if a specified user is currently on cooldown.
     *
     * @param user the unique identifier of the user whose cooldown status needs to be checked; cannot be null.
     * @return {@code true} if the user is on cooldown, otherwise {@code false}.
     */
    public boolean isOnCooldown(String user) {
        return cooldowns.containsKey(user) && System.currentTimeMillis() < cooldowns.get(user);
    }

    /**
     * Retrieves the remaining cooldown time for a specific user.
     * If the user is not on cooldown, the method returns 0.
     *
     * @param user The username for which the remaining cooldown time is being checked.
     * @return The remaining cooldown time in seconds, or 0 if the user is not on cooldown.
     */
    public long getRemainingTime(String user) {
        return isOnCooldown(user) ? (cooldowns.get(user) - System.currentTimeMillis()) / 1000 : 0;
    }

    /**
     * Sets a cooldown for the specified user. The cooldown duration is determined
     * based on the predefined cooldown time and the current system time.
     *
     * @param user the unique identifier of the user for whom the cooldown is being set; cannot be null.
     */
    public void setCooldown(String user) {
        cooldowns.put(user, System.currentTimeMillis() + cooldownTime);
    }

    /**
     * Removes the cooldown for a specified user, allowing them to take actions immediately.
     *
     * @param user the unique identifier of the user whose cooldown should be removed; cannot be null.
     */
    public void removeCooldown(String user) {
        cooldowns.remove(user);
    }

    /**
     * Sets the cooldown time for all users. The provided time is converted from seconds to milliseconds.
     *
     * @param cooldownTime The cooldown time in seconds to be set. Must be a positive value.
     */
    public void setCooldownTime(long cooldownTime) {
        this.cooldownTime = cooldownTime * 1000;
    }
}