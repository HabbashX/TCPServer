package com.habbashx.tcpserver.event;

/**
 * Represents the priority levels that can be assigned to event listeners or handlers.
 * This enum is primarily used to determine the order in which listeners are invoked
 * when handling events. Higher priority levels imply earlier execution.
 *
 * The priority levels, in ascending order of precedence, are:
 * - LOW: Assigned the lowest precedence, typically the default value.
 * - NORMAL: Represents the standard or default priority level.
 * - HIGH: Indicates a higher precedence than LOW and NORMAL.
 * - HIGHEST: Assigned the second-highest precedence level.
 * - MONITOR: Primarily utilized for logging or monitoring purposes and is not intended
 *   to directly influence event flow.
 *
 * Each priority level is associated with an integer value, which can be retrieved
 * using the {@link #getPriority()} method.
 */
public enum Priority {

    LOW(0x00),
    NORMAL(0x01),
    HIGH(0x02),
    HIGHEST(0x03),
    MONITOR(0x04); // just for logging

    private final int priority;

    Priority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
