package com.habbashx.tcpserver.listener;

import com.habbashx.tcpserver.event.Event;
import com.habbashx.tcpserver.event.manager.EventManager;

/**
 * Represents a generic event listener that can handle a specific type of event.
 *
 * <p>
 * Implementations of this interface are responsible for defining the logic
 * to be executed when a particular event occurs. Each listener explicitly
 * declares the type of event it handles via {@link #getEventType()}, which
 * allows the event system to avoid runtime reflection and perform efficient
 * dispatching.
 * </p>
 *
 * <p>
 * This interface works in conjunction with the {@link EventManager}, which
 * manages registration, prioritization, and execution of listeners.
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * public class UserChatListenerHandler implements Listener<UserChatEvent> {
 *
 *     @Override
 *     public Class<UserChatEvent> getEventType() {
 *         return UserChatEvent.class;
 *     }
 *
 *     @Override
 *     public void onEvent(UserChatEvent event) {
 *         System.out.println(event.getUsername()+": "+event.getMessage());
 *     }
 * }
 * }</pre>
 *
 * @param <E> the type of event this listener handles
 */
public interface Listener<E extends Event> extends EventListenerConfiguration {

    /**
     * Returns the class type of the event this listener is designed to handle.
     *
     * <p>
     * This method eliminates the need for runtime generic type inspection,
     * enabling the event system to directly map listeners to their corresponding
     * event types during registration.
     * </p>
     *
     * @return the class object representing the event type
     */
    Class<E> getEventType();

    /**
     * Handles the given event.
     *
     * <p>
     * This method is invoked by the {@link EventManager} when an event of the
     * corresponding type is triggered. The implementation should contain the
     * logic required to respond to the event.
     * </p>
     *
     * @param event the event instance to handle
     */
    void onEvent(E event);
}