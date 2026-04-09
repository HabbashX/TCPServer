package com.habbashx.tcpserver.event.manager;

import com.habbashx.tcpserver.event.Event;
import com.habbashx.tcpserver.event.Priority;
import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.listener.Listener;
import com.habbashx.tcpserver.socket.server.Server;
import com.habbashx.tcpserver.socket.server.foundation.ServerFoundation;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The EventManager is responsible for managing event listeners and dispatching events
 * to the appropriate listeners in an efficient and scalable manner.
 *
 * <p>
 * This implementation is optimized to avoid runtime reflection by requiring listeners
 * to explicitly declare their event type via {@link Listener#getEventType()}. This allows
 * the manager to precompute and index listeners by event type during registration,
 * resulting in fast event dispatching.
 * </p>
 *
 * <p>
 * Features:
 * <ul>
 *     <li>Efficient event dispatching using direct lookup (O(1) per event type)</li>
 *     <li>Support for listener prioritization</li>
 *     <li>Optional asynchronous event handling</li>
 *     <li>Inheritance-aware event propagation</li>
 *     <li>Thread-safe listener storage</li>
 * </ul>
 * </p>
 */
public class EventManager {

    private final ServerFoundation serverFoundation;

    /**
     * A mapping between event types and their corresponding registered listeners.
     *
     * <p>
     * Each event class maps to a list of {@link RegisteredListener} instances
     * that should be invoked when that event is triggered.
     * </p>
     */
    private final Map<Class<?>, List<RegisteredListener<?>>> registeredListeners = new ConcurrentHashMap<>();

    /**
     * Executor used for handling asynchronous event execution.
     *
     * <p>
     * This implementation uses virtual threads (Project Loom) to efficiently
     * handle a large number of concurrent tasks with minimal overhead.
     * </p>
     */
    private final ExecutorService asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();


    /**
     * Constructs a new EventManager instance.
     *
     * @param serverFoundation the server foundation providing logging and core services
     */
    public EventManager(ServerFoundation serverFoundation) {
        this.serverFoundation = serverFoundation;
    }

    /**
     * Registers a listener with the event manager.
     *
     * <p>
     * The listener must be annotated with {@link EventHandler}. During registration,
     * its event type, priority, and execution mode are extracted and stored for
     * efficient future dispatch.
     * </p>
     *
     * @param listener the listener to register
     * @param <E>      the type of event the listener handles
     */
    public <E extends Event> void registerEvent(@NotNull Listener<E> listener) {

        final Class<?> clazz = listener.getClass();

        if (!clazz.isAnnotationPresent(EventHandler.class)) {
            serverFoundation.getServerLogger().warning("""
                    this listener %s is missing the @EventHandler annotation
                    listener %s will not be listening for specific event until properly annotated with @EventHandler
                    """.formatted(clazz.getSimpleName(), clazz.getSimpleName()));
            return;
        }

        final EventHandler annotation = clazz.getAnnotation(EventHandler.class);
        final boolean async = annotation.isAsync();
        final int priority = annotation.priority().getPriority();

        for (final Type type : clazz.getGenericInterfaces()) {
            if (type instanceof final ParameterizedType pt) {

                final Type actual = pt.getActualTypeArguments()[0];

                if (actual instanceof final Class<?> eventClass) {

                    final RegisteredListener<E> reg =
                            new RegisteredListener<>(listener, async, priority);

                    registeredListeners
                            .computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                            .add(reg);

                    registeredListeners.get(eventClass)
                            .sort(Comparator.comparingInt((RegisteredListener<?> l) -> l.priority).reversed());
                    logEventRegistration(listener, eventClass, priority);
                }
            }

        }


    }

    /**
     * Unregisters a listener by its class name.
     *
     * <p>
     * All listeners matching the provided class simple name will be removed
     * from all event mappings.
     * </p>
     *
     * @param listenerName the simple class name of the listener to remove
     */
    public void unregisterEvent(final String listenerName) {
        for (final List<RegisteredListener<?>> list : registeredListeners.values()) {
            list.removeIf(l -> l.listener.getClass().getSimpleName().equals(listenerName));
        }
    }


    /**
     * Triggers an event and dispatches it to all relevant listeners.
     *
     * <p>
     * The method performs a lookup based on the event's class and iterates through
     * all matching listeners. If inheritance is present, parent event types are
     * also considered.
     * </p>
     *
     * <p>
     * If the event is cancelled, further propagation is stopped immediately.
     * </p>
     *
     * @param event the event to trigger
     * @param <E>   the type of the event
     */
    @SuppressWarnings("unchecked")
    public <E extends Event> void triggerEvent(@NotNull final E event) {

        if (event.isCancelled()) return;

        Class<?> clazz = event.getClass();

        while (clazz != null) {

            final List<RegisteredListener<?>> listeners = registeredListeners.get(clazz);

            if (listeners != null) {
                for (final RegisteredListener<?> reg : listeners) {

                    if (event.isCancelled()) return;

                    final RegisteredListener<E> listener = (RegisteredListener<E>) reg;

                    if (listener.async) {
                        asyncExecutor.submit(() -> listener.listener().onEvent(event));
                    } else {
                        listener.listener().onEvent(event);
                    }
                }
            }

            clazz = clazz.getSuperclass();

        }
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

    private void logEventRegistration(Listener<?> listener, Class<?> eventClass, int priority) {

        if (((Server) serverFoundation).isEventRegistrationLogging()) {
            serverFoundation.getServerLogger().info(
                    "Registered listener: "
                            + listener.getClass().getSimpleName()
                            + " | Event: "
                            + eventClass.getSimpleName()
                            + " | Priority: "
                            + priority
            );
        }
    }

    /**
     * Returns the internal mapping of registered listeners.
     *
     * <p>
     * This method is primarily intended for debugging or inspection purposes.
     * </p>
     *
     * @return a map of event types to their corresponding listeners
     */
    public Map<Class<?>, List<RegisteredListener<?>>> getRegisteredListeners() {
        return registeredListeners;
    }

    /**
     * Internal wrapper record class that stores precomputed metadata for a listener.
     *
     * <p>
     * This avoids repeated reflection and annotation lookups during event dispatch.
     * </p>
     *
     * @param <E> the type of event handled by the listener
     */
    private record RegisteredListener<E extends Event>(Listener<E> listener, boolean async, int priority) {
    }
}
