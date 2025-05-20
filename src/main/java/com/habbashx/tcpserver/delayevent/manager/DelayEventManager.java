package com.habbashx.tcpserver.delayevent.manager;

import com.habbashx.tcpserver.delayevent.DelayEvent;
import com.habbashx.tcpserver.delayevent.handler.DelayEventHandler;

import com.habbashx.tcpserver.listener.DelayListener;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * DelayEventManager is responsible for managing and triggering events with
 * associated delays. It facilitates registering, unregistering, and triggering
 * events that are associated with listeners annotated with {@code DelayEventHandler}.
 *
 * This class ensures listeners are executed according to their priority and delay
 * intervals, using a scheduled executor service.
 */
public class DelayEventManager {

    private final Server server;
    /**
     * A single-threaded scheduled executor service used to handle delayed execution of events.
     * This ensures that tasks are executed in a sequential and thread-safe manner.
     * It is primarily utilized for managing and triggering delayed events within the
     * DelayEventManager class.
     */
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    /**
     * A list that stores all the registered delay listeners for handling delay-based events.
     *
     * This list maintains the listeners that implement the {@link DelayListener} interface.
     * The listeners are registered to handle specific {@link DelayEvent} instances.
     *
     * It is utilized internally by the {@code DelayEventManager} class for the following:
     * - Registering new listeners.
     * - Unregistering existing listeners.
     * - Sorting listeners based on priority or delay.
     * - Triggering events on all relevant listeners.
     *
     * The list is declared as final to ensure its reference cannot be changed,
     * providing a consistent collection of listeners throughout the lifecycle of the
     * {@code DelayEventManager} instance.
     */
    private final List<DelayListener<?>> registeredListeners = new ArrayList<>();

    public DelayEventManager(Server server) {
        this.server = server;
    }

    /**
     * Registers an event listener for handling delayed events. The listener must be annotated with
     * {@code @DelayEventHandler} to be successfully registered. If the annotation is missing,
     * the listener will not be registered and a warning will be logged.
     *
     * @param listener the event listener to register, must not be null and must be annotated with {@code @DelayEventHandler}
     */
    public <E extends DelayEvent> void registerEvent(@NotNull DelayListener<E> listener) {

        if (listener.getClass().isAnnotationPresent(DelayEventHandler.class)) {

            registeredListeners.add(listener);
            sortListeners();
        } else {
            server.getServerLogger().error("""
                    the listener %s is missing the annotation @DelayEventHandler
                    Please ensure that listener is annotated with @DelayEventHandler
                    the listener %s will not be listening for specific events until properly annotated.
                    """.formatted(listener, listener));
        }
    }

    /**
     * Unregisters an event listener by its class name.
     *
     * This method removes the listener from the registeredListeners collection
     * if the class name of the listener matches the specified listener name.
     *
     * @param listenerName the simple name of the listener's class to be unregistered
     */
    public void unregisterEvent(String listenerName) {
        registeredListeners.removeIf(listener -> listener.getClass().getSimpleName().equals(listenerName));
    }


    /**
     * Triggers an event and dispatches it to all registered listeners compatible with the event's type.
     * The listeners execute the event handling logic with the specified delay.
     * This method utilizes scheduled tasks to respect the delay configuration of each listener.
     *
     * @param <E>   the type of the event, which must extend {@code DelayEvent}
     * @param event the event to be triggered, not null
     */
    @SuppressWarnings("unchecked")
    public <E extends DelayEvent> void triggerEvent(E event) {

        for (DelayListener<?> listener : registeredListeners) {
            Type[] typeArguments = listener.getClass().getGenericInterfaces();


            for (Type typeArgument : typeArguments) {
                if (typeArgument instanceof ParameterizedType) {

                    Type actualType = ((ParameterizedType) typeArgument).getActualTypeArguments()[0];
                    if (actualType instanceof Class<?> && ((Class<?>) actualType).isAssignableFrom(event.getClass())) {

                        final long delay = getDelay(listener);
                        Runnable eventTask = () -> ((DelayListener<E>) listener).onEvent(event);
                        executor.scheduleAtFixedRate(eventTask,0,delay,TimeUnit.MILLISECONDS);
                    }
                }
            }
        }
    }

    /**
     * Sorts the registered event listeners based on their priority.
     *
     * This method organizes the {@code registeredListeners} list by arranging the listeners
     * in ascending order of their priority. The priority is determined by invoking the {@code getPriority}
     * method, which retrieves the priority value from the {@code @DelayEventHandler} annotation
     * on the listener's class.
     *
     * It ensures that listeners with higher precedence (lower priority values) are processed
     * before those with lower precedence (higher priority values) during event handling.
     *
     * This method is called internally after a new listener is registered to maintain the
     * correct order of execution among all registered listeners.
     */
    private void sortListeners() {
        registeredListeners.sort(Comparator.comparingInt(this::getPriority));
    }

    /**
     * Determines the delay in milliseconds for a given event listener by retrieving
     * the value specified in the {@code @DelayEventHandler} annotation on the listener's class.
     *
     * @param listener the event listener whose delay is to be determined; must not be null
     *                 and must have a {@code @DelayEventHandler} annotation present on its class.
     * @return the delay in milliseconds as specified in the {@code delayMilliSeconds}
     *         attribute of the {@code @DelayEventHandler} annotation.
     */
    private long getDelay(@NotNull DelayListener<?> listener) {
        return listener.getClass().getAnnotation(DelayEventHandler.class).delayMilliSeconds();
    }

    /**
     * Retrieves the priority of the specified delay event listener. The priority is determined
     * based on the {@code @DelayEventHandler} annotation applied to the listener's class.
     *
     * @param listener the delay event listener whose priority should be retrieved; must not be null and
     *                 must be annotated with {@code @DelayEventHandler}.
     * @return the priority value of the listener as defined in the {@code @DelayEventHandler} annotation.
     *         A higher priority value indicates higher precedence during event processing.
     * @throws NullPointerException if the listener is null or the {@code @DelayEventHandler} annotation is missing.
     */
    private int getPriority(@NotNull DelayListener<?> listener) {
        return listener.getClass().getAnnotation(DelayEventHandler.class).priority().getPriority();
    }

    /**
     * Retrieves the list of registered listeners that handle delayed events.
     * The returned list contains all listeners which have been successfully registered
     * and are managed by this instance of the DelayEventManager.
     *
     * @return a list of registered {@code DelayListener} objects managing delayed events
     */
    public List<DelayListener<?>> getRegisteredListeners() {
        return registeredListeners;
    }
}