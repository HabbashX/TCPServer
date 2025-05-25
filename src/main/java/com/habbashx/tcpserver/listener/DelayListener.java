package com.habbashx.tcpserver.listener;

import com.habbashx.tcpserver.configuration.Configuration;
import com.habbashx.tcpserver.configuration.JsonConfiguration;
import com.habbashx.tcpserver.delayevent.DelayEvent;
import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a listener interface for handling events with a delay mechanism.
 * This interface is intended to be implemented by classes that process delayed events
 * of a specific type, where the event type must extend {@link DelayEvent}.
 *
 * The listener's primary responsibility is to define the behavior that should occur
 * when the delayed event is triggered. This behavior is implemented in the method.
 *
 * Characteristics:
 * - The event type {@code E} must be a subclass of {@link DelayEvent}.
 * - Typically used in an event management system that provides facilities for scheduling
 *   and executing delayed events.
 * - Implementing classes should be annotated with {@code @DelayEventHandler} to enable
 *   proper registration and delay configuration.
 *
 * Responsibilities:
 * - Listen to specific types of delayed events.
 * - Execute custom business logic when the delayed event is triggered.
 *
 * Common Usage:
 * - Create a class implementing {@code DelayListener} for a specific {@link DelayEvent} subclass.
 * - Annotate the implementing class with {@code @DelayEventHandler} to specify delay
 *   and prioritization.
 * - Register the listener with an event manager to handle corresponding delayed events.
 *
 * @param <E> the type of the event handled by the listener, must extend {@link DelayEvent}
 */
public interface DelayListener<E extends DelayEvent> {
    void onEvent(E event);

    default Configuration loadConfiguration(Server server) {
        final String configFile = getConfigFile();
        assert configFile != null;
        return new JsonConfiguration(configFile ,server);
    }

    default @Nullable String getConfigFile() {
        return this.getClass().getAnnotation(EventHandler.class).configFile();
    }

}
