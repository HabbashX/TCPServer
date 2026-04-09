package com.habbashx.tcpserver.delayevent.manager;

import com.habbashx.tcpserver.delayevent.DelayEvent;
import com.habbashx.tcpserver.delayevent.handler.DelayEventHandler;
import com.habbashx.tcpserver.listener.DelayListener;
import com.habbashx.tcpserver.socket.server.Server;
import com.habbashx.tcpserver.socket.server.foundation.ServerFoundation;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * The DelayEventManager is responsible for managing and dispatching delayed events
 * to registered listeners in an efficient and scalable manner.
 *
 * <p>
 * Unlike traditional event managers, this implementation supports delayed execution,
 * allowing listeners to handle events after a specified time interval. Each listener
 * defines its delay and priority via the {@link DelayEventHandler} annotation.
 * </p>
 *
 * <p>
 * This implementation is optimized for performance by:
 * <ul>
 *     <li>Avoiding runtime reflection during event dispatch</li>
 *     <li>Precomputing listener metadata during registration</li>
 *     <li>Indexing listeners by event type for fast lookup</li>
 *     <li>Using a scheduled executor for controlled delayed execution</li>
 * </ul>
 * </p>
 *
 * <p>
 * The manager also supports event inheritance, meaning that listeners registered
 * for a parent event class will also receive events of its subclasses.
 * </p>
 */
public class DelayEventManager {

    private final ServerFoundation serverFoundation;

    /**
     * A mapping between event types and their corresponding delayed listeners.
     *
     * <p>
     * Each key represents an event class, and its value is a list of listeners
     * that should be executed when that event (or its subclass) is triggered.
     * </p>
     */
    private final Map<Class<?>, List<RegisteredDelayListener<?>>> listenerMap = new ConcurrentHashMap<>();

    /**
     * A single-threaded scheduled executor responsible for executing delayed tasks.
     *
     * <p>
     * Tasks submitted to this executor are executed sequentially in a dedicated thread,
     * ensuring predictable execution order and avoiding race conditions for delayed events.
     * </p>
     */
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * Constructs a new DelayEventManager instance.
     *
     * @param serverFoundation the server foundation providing logging and core services
     */
    public DelayEventManager(final @NotNull ServerFoundation serverFoundation) {
        this.serverFoundation = serverFoundation;
    }


    /**
     * Registers a delay listener with the event manager.
     *
     * <p>
     * The listener must be annotated with {@link DelayEventHandler}. During registration,
     * its delay, priority, and event type are extracted and stored for efficient dispatching.
     * </p>
     *
     * @param listener the listener to register
     * @param <E>      the type of delayed event handled by the listener
     */
    public <E extends DelayEvent> void registerEvent(@NotNull final DelayListener<E> listener) {


        final Class<?> clazz = listener.getClass();

        if (!clazz.isAnnotationPresent(DelayEventHandler.class)) {
            serverFoundation.getServerLogger().warning("""
                    the listener %s is missing the annotation @DelayEventHandler
                    """.formatted(clazz.getSimpleName()));
            return;
        }

        final DelayEventHandler annotation = clazz.getAnnotation(DelayEventHandler.class);

        final long delay = annotation.delayMilliSeconds();
        final int priority = annotation.priority().getPriority();

        final Class<?> eventClass = listener.getEventType();

        final RegisteredDelayListener<E> reg =
                new RegisteredDelayListener<>(listener, delay, priority);

        listenerMap
                .computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                .add(reg);

        listenerMap.get(eventClass)
                .sort(Comparator.comparingInt((RegisteredDelayListener<?> l) -> l.priority));

        logDelayEventRegisteration(listener, eventClass, delay, priority);
    }


    /**
     * Unregisters a delay listener by its class name.
     *
     * <p>
     * All listeners with a matching class simple name will be removed from all
     * event mappings.
     * </p>
     *
     * @param listenerName the simple class name of the listener to remove
     */
    public void unregisterEvent(final String listenerName) {
        for (final List<RegisteredDelayListener<?>> list : listenerMap.values()) {
            list.removeIf(l -> l.listener.getClass().getSimpleName().equals(listenerName));
        }
    }

    /**
     * Triggers a delayed event and schedules its execution for all relevant listeners.
     *
     * <p>
     * Each listener is scheduled to execute once after its configured delay.
     * This method performs a lookup based on the event type and also considers
     * its superclass hierarchy to support inheritance.
     * </p>
     *
     * <p>
     * Execution is handled asynchronously by the internal {@link ScheduledExecutorService}.
     * </p>
     *
     * @param event the event to trigger
     * @param <E>   the type of the delayed event
     */
    @SuppressWarnings("unchecked")
    public <E extends DelayEvent> void triggerEvent(final @NotNull E event) {

        Class<?> clazz = event.getClass();

        while (clazz != null) {

            final List<RegisteredDelayListener<?>> listeners = getRegisteredListeners().get(clazz);

            if (listeners != null) {
                for (final RegisteredDelayListener<?> reg : listeners) {

                    final RegisteredDelayListener<E> listener = (RegisteredDelayListener<E>) reg;

                    executor.schedule(
                            () -> listener.listener.onEvent(event),
                            listener.delay,
                            TimeUnit.MILLISECONDS
                    );
                }
            }

            clazz = clazz.getSuperclass();
        }
    }

    private void logDelayEventRegisteration(DelayListener<?> listener, Class<?> eventClass, long delay, int priority) {

        if (((Server) serverFoundation).isDelayEventRegistrationLogging()) {
            serverFoundation.getServerLogger().info(
                    "Registered delay listener: "
                            + listener.getClass().getSimpleName()
                            + " | Event: "
                            + eventClass.getSimpleName()
                            + " | Delay: "
                            + delay
                            + "ms | Priority: "
                            + priority
            );
        }

    }

    /**
     * Retrieves the internal mapping of registered delay listeners.
     *
     * <p>
     * This method is primarily intended for debugging or inspection purposes.
     * </p>
     *
     * @return a map of event types to their corresponding delayed listeners
     */
    public Map<Class<?>, List<RegisteredDelayListener<?>>> getRegisteredListeners() {
        return listenerMap;
    }

    /**
     * Internal wrapper class that stores precomputed metadata for delayed listeners.
     *
     * <p>
     * This avoids repeated reflection and annotation lookups during event dispatch,
     * significantly improving performance.
     * </p>
     *
     * @param <E>      the type of event handled by the listener
     * @param listener The listener instance
     * @param delay    The delay in milliseconds before execution
     * @param priority The priority of the listener
     */
    private record RegisteredDelayListener<E extends DelayEvent>(DelayListener<E> listener, long delay, int priority) {
    }

}