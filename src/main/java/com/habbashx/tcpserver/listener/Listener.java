package com.habbashx.tcpserver.listener;

import com.habbashx.tcpserver.event.Event;

/**
 * The Listener interface represents a generic contract for handling events of a specific type.
 * It serves as a base abstraction for all event listeners, allowing the implementation of custom
 * event-handling logic for any event subtype.
 *
 * @param <E> the type of event that this listener handles; must extend from the base {@link Event} class
 *
 * Responsibilities:
 * - Defines the behavior for responding to an event via the {@code onEvent(E event)} method.
 * - Allows implementations to specialize event handling for various types of events.
 *
 * Key Method:
 * - {@code onEvent(E event)}:
 *   - This method is invoked when an event of type {@code E} occurs.
 *   - Implementing classes should override this method to provide the specific event-handling logic.
 *
 * Design Details:
 * - The Listener interface is parameterized with a generic type, ensuring type safety when handling events.
 * - This interface extends {@link EventListenerConfiguration}, which may provide additional configuration
 *   options or behaviors for event listeners.
 *
 * Usage:
 * - Implementing classes must specify the type of event they handle by defining the generic parameter {@code E}.
 * - The implementation of the {@code onEvent(E event)} method contains the custom logic for processing the specified event type.
 *
 * Thread-Safety:
 * - The thread-safety of the implementation depends on the specific use case and event system.
 * - Implementers should consider synchronization or concurrency mechanisms if required.
 */
public interface Listener<E extends Event> extends EventListenerConfiguration {

    void onEvent(E event);
}
