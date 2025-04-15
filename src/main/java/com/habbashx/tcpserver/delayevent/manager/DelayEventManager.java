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

public class DelayEventManager {

    private final Server server;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final List<DelayListener<?>> registeredListeners = new ArrayList<>();

    public DelayEventManager(Server server) {
        this.server = server;
    }

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

    public void unregisterEvent(String listenerName) {
        registeredListeners.removeIf(listener -> listener.getClass().getSimpleName().equals(listenerName));
    }


    @SuppressWarnings("unchecked")
    public <E extends DelayEvent> void triggerEvent(E event) {
        // Warning do not modify any line of code in this method,
        // only if you understand what`s going on right here ok ?,
        // if you want to add anything do you want additional features optimizing code feel free to modify it :D,
        // see LICENCE arguments.

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

    private void sortListeners() {
        registeredListeners.sort(Comparator.comparingInt(this::getPriority));
    }

    private long getDelay(@NotNull DelayListener<?> listener) {
        return listener.getClass().getAnnotation(DelayEventHandler.class).delayMilliSeconds();
    }

    private int getPriority(@NotNull DelayListener<?> listener) {
        return listener.getClass().getAnnotation(DelayEventHandler.class).priority().getPriority();
    }

    public List<DelayListener<?>> getRegisteredListeners() {
        return registeredListeners;
    }
}