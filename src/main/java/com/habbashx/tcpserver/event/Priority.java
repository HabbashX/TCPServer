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

    /**
     * The LOW priority level represents the lowest precedence for event listeners or
     * handlers. It is typically used as the default value and signifies that the
     * associated listener or handler should be executed later compared to those
     * with higher priority levels.
     */
    LOW(0x00),
    /**
     * Represents the standard or default priority level```java
     /**
     * Represents the standard or default priority level for event listeners or handlers.
     * NORMAL priority signifies a baseline precedence, typically used in most cases
     * where no higher or lower priority is explicitly required.
     */
    NORMAL(0x01),
    /**
     * Represents the priority level "HIGH" for event listeners or handlers.
     * This priority level is higher than LOW and NORMAL, ensuring that listeners assigned
     * this level are invoked earlier during event handling compared to those assigned lower priorities.
     * It is associated with the integer value 0x02.
     */
    HIGH(0x02),
    /**
     * Represents the second-highest priority level that can be assigned to event listeners.
     * Event listeners with this priority are executed before those with lower priority levels
     * (LOW, NORMAL, HIGH) and after the MONITOR level.
     */
    HIGHEST(0x03),
    /**
     * Represents the "Monitor" priority level within the enumeration.
     * This priority level is primarily intended for logging or monitoring purposes
     * and does not directly influence the order of event flow.
     * It is assigned the highest numerical value among the defined priorities.
     */
    MONITOR(0x04); // just for logging

    /**
     * Represents the priority level associated with a specific priority enumeration constant.
     * This variable defines the numeric value assigned to each priority level, determining
     * the precedence of execution for event listeners or handlers.
     *
     * Higher integer values indicate higher precedence, meaning listeners or handlers
     * with higher priority values are executed earlier during event processing.
     */
    private final int priority;

    /**
     * Constructor for the Priority enum that associates a numerical value
     * with a specific priority level. This numerical value determines the
     * precedence of the priority level in the context of event handling.
     *
     * @param priority the integer value representing the precedence of the priority level
     */
    private Priority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
