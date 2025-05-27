package com.habbashx.tcpserver.listener;

import com.habbashx.tcpserver.delayevent.DelayEvent;
import com.habbashx.tcpserver.delayevent.handler.DelayEventHandler;
import com.habbashx.tcpserver.delayevent.manager.DelayEventManager;

/**
 * This interface serves as a contract for event listeners that handle delayed events.
 * The events must extend the {@link DelayEvent} class, and implementing classes are
 * expected to define the logic for processing those events with a delay.
 *
 * Responsibilities:
 * - Defines a type-safe mechanism for handling delayed events of a specific type.
 * - Ensures that implementing classes provide an implementation of the {@code onEvent(E event)} method
 *   to process the event logic.
 *
 * Generic Type:
 * - {@code <E>} is a generic type parameter that must extend {@link DelayEvent}, ensuring that the listener
 *   handles only delayed events.
 *
 * Common Use Cases:
 * - This interface is commonly implemented in event-driven systems requiring scheduled or delayed execution
 *   of specific actions.
 * - Used by the {@code DelayEventManager} to register, manage, and execute delayed event listeners.
 *
 * Integration with Annotations:
 * - Implementing classes are typically annotated with {@code @DelayEventHandler}, which defines
 *   configuration such as the delay in milliseconds and the priority level of the processing.
 *
 * See Also:
 * - {@link DelayEvent}
 * - {@link DelayEventHandler}
 * - {@link DelayEventManager}
 *
 * @param <E> the type of delayed event, which must extend {@link DelayEvent}.
 */
public interface DelayListener<E extends DelayEvent> extends EventListenerConfiguration{

    /**
     * Handles the processing of a delayed event of a specific type.
     *
     * This method is invoked when an event of type {@code E} is triggered, allowing the
     * implementing listener to define custom logic for handling the delayed event.
     * The method ensures that the event is processed after the delay specified in
     * the listener's {@code @DelayEventHandler} annotation.
     *
     * @param event the event to be processed, must not be null. The event should extend
     *              {@link DelayEvent}, and its type is matched to the listener's type
     *              during event dispatching to ensure compatibility.
     */
    void onEvent(E event);
}
