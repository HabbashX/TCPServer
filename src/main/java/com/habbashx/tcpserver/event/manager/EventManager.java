package com.habbashx.tcpserver.event.manager;

import com.habbashx.tcpserver.event.Event;
import com.habbashx.tcpserver.event.Priority;
import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.listener.Listener;
import com.habbashx.tcpserver.socket.server.foundation.ServerFoundation;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The EventManager class is responsible for managing event listeners and triggering events in the system.
 * It handles the registration, unregistration, sorting, and execution of listeners for various event types.
 * It also ensures that events can be processed either synchronously or asynchronously based on their configuration.
 */
public class EventManager {

    private final ServerFoundation serverFoundation;

    /**
     * A list that holds all the listeners registered to the EventManager.
     * Each listener in this list implements the {@link Listener} interface
     * and reacts to specific types of events defined by the generic type parameter.
     * <p>
     * The {@code registeredListeners} collection is used internally to manage
     * event callbacks and to dispatch events to the appropriate listeners.
     * <p>
     * This field is immutable and initialized as an {@code ArrayList}. Elements
     * can be added or removed through methods provided by the EventManager class.
     */
    private final List<Listener<?>> registeredListeners = new ArrayList<>();

    /**
     * An ExecutorService instance used for asynchronous operations within the EventManager.
     * This executor operates using a cached thread pool, allowing for efficient handling of
     * tasks with dynamically growing or shrinking thread usage based on demand.
     * It is utilized for executing event-related tasks in a non-blocking manner.
     */
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();


    public EventManager(ServerFoundation serverFoundation) {
        this.serverFoundation = serverFoundation;
    }

    /**
     * Registers an event listener to the event manager. The method checks if the listener
     * is annotated with {@code @EventHandler}. If the annotation is present, the listener
     * is added to the internal list of registered listeners, and the list is sorted based
     */
    public <E extends Event> void registerEvent(@NotNull Listener<E> listener) {

        if (listener.getClass().isAnnotationPresent(EventHandler.class)) {
            registeredListeners.add(listener);
            sortListeners();
        } else {
            serverFoundation.getServerLogger().warning("""
                    this listener %s is missing the @EventHandler annotation
                    listener %s will not be listening for specific event until properly annotated with @EventHandler
                    """);
        }

    }

    /**
     * Unregisters an event listener from the list of registered listeners.
     *
     * @param listenerName the name of the listener to be removed from the registered listeners
     */
    public void unregisterEvent(String listenerName) {
        registeredListeners.removeIf(listener -> listener.getClass().getSimpleName().equals(listenerName));
        sortListeners();
    }

    /**
     * Triggers an event by notifying all registered listeners capable of handling the provided event type.
     * The method iterates through the list of registered listeners, checks if a listener is designed to handle
     * the specified event type, and invokes the listener's event handling logic based on its annotation configuration.
     * If the event was marked as cancelled, no further listeners are notified, and the method returns immediately.
     * Event handling can be synchronous or asynchronous depending on the {@code EventHandler} annotation's configuration.
     *
     * @param <E>   the type of the event being triggered, which must extend the {@code Event} class
     * @param event the event object to be triggered and passed to applicable listeners
     */
    @SuppressWarnings("unchecked")
    public <E extends Event> void triggerEvent(E event) {

        for (Listener<?> listener : registeredListeners) {
            Type[] typeArguments = listener.getClass().getGenericInterfaces();


            for (Type typeArgument : typeArguments) {
                if (typeArgument instanceof ParameterizedType) {

                    Type actualType = ((ParameterizedType) typeArgument).getActualTypeArguments()[0];
                    if (actualType instanceof Class<?> && ((Class<?>) actualType).isAssignableFrom(event.getClass())) {
                        EventHandler eventHandler = listener.getClass().getAnnotation(EventHandler.class);

                        if (!event.isCancelled()) {
                            if (eventHandler.isAsync()) {
                                asyncExecutor.submit(() -> {
                                    ((Listener<E>) listener).onEvent(event);
                                });
                            } else {
                                ((Listener<E>) listener).onEvent(event);
                            }
                        } else {
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Sorts the list of registered listeners based on their priority in descending order.
     * <p>
     * The priority of each listener is determined using the {@code getPriority} method,
     * which retrieves the priority level from the {@code @EventHandler} annotation associated
     * with the listener. If no priority is specified within the annotation, a default priority
     * level is used. This method ensures that listeners with higher priority values are
     * positioned earlier in the list.
     * <p>
     * This method is invoked internally after adding or removing listeners from the list
     * to maintain the correct execution order of listeners when events are triggered.
     */
    private void sortListeners() {

        registeredListeners.sort(Comparator.comparingInt(this::getPriority).reversed());
    }

    /**
     * Determines the priority of the given listener based on its {@code @EventHandler} annotation.
     * If the listener's class is annotated with {@link EventHandler}, it retrieves the priority
     * set in the annotation. If the annotation is not present, a default priority of {@code NORMAL}
     * will be returned.
     *
     * @param listener the listener whose priority is to be determined; must not be null
     * @return the priority value associated with the listener
     */
    private int getPriority(@NotNull Listener<?> listener) {
        EventHandler annotation = listener.getClass().getAnnotation(EventHandler.class);
        return (annotation != null) ? annotation.priority().getPriority() : Priority.NORMAL.getPriority();
    }

    public List<Listener<?>> getRegisteredListeners() {
        return registeredListeners;
    }
}
