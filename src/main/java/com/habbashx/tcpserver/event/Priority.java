package com.habbashx.tcpserver.event;

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
