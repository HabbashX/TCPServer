package com.habbashx.tcpserver.listener;

import com.habbashx.tcpserver.event.Event;

/**
 * Represents a generic listener interface for handling events of a specific type.
 * Classes implementing this interface are responsible for performing actions in
 * response to the occurrence of events.
 *
 * This is a parameterized interface where the type parameter {@code E} extends
 * the {@link Event} class, ensuring type safety for the events handled by the listener.
 *
 * Responsibilities:
 * - Provide a method for processing events of the specified type.
 * - Serve as the foundation for creating specific event-handling implementations.
 *
 * Design Notes:
 * - The {@code Listener} interface enables a decoupled architecture by allowing
 *   event producers and consumers to interact via this common interface without
 *   being tightly coupled.
 *
 * Type Parameter:
 * - {@code E}: The type of event that this listener can handle. It must be a subclass of {@link Event}.
 *
 * Method Summary:
 * - {@code void onEvent(E event)}:
 *   Defines the action to be performed when an event of type {@code E} occurs.
 *
 * Contract:
 * - Implementing classes are expected to define the behavior of event handling in
 *   the {@code onEvent} method depending on the specific event type.
 *
 * Usage:
 * - This interface is commonly used within event-driven systems where specialized
 *   handlers respond to various system events.
 *
 * Example Implementations:
 * - AuthenticationEventHandler
 * - DefaultUserJoinHandler
 * - DefaultMutedUserHandler
 * - DefaultUserLeaveHandler
 * - DefaultUserExecuteCommandHandler
 */
public interface Listener<E extends Event> {
    void onEvent(E event);
}
