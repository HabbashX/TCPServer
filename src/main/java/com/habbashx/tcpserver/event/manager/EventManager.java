package com.habbashx.tcpserver.event.manager;

import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.event.Event;
import com.habbashx.tcpserver.event.Priority;

import com.habbashx.tcpserver.listener.Listener;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventManager {

    private final Server server;

    private final List<Listener<?>> registeredListeners = new ArrayList<>();

    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();


    public EventManager(Server server) {
        this.server = server;
    }

    public <E extends Event> void registerEvent(@NotNull Listener<E> listener) {

        if (listener.getClass().isAnnotationPresent(EventHandler.class)) {
            registeredListeners.add(listener);
            sortListeners();
        } else {
            server.getServerLogger().error("""
                    this listener %s is missing the @EventHandler annotation
                    listener %s will not be listening for specific event until properly annotated with @EventHandler
                    """);
        }

    }

    public void unregisterEvent(String listenerName) {
        registeredListeners.removeIf(listener -> listener.getClass().getSimpleName().equals(listenerName));
        sortListeners();
    }

    @SuppressWarnings("unchecked")
    public <E extends Event> void triggerEvent(E event) {
        // Warning do not modify any line of code in this method,
        // only if you understand what`s going on right here ok ?,
        // if you want to add anything do you want additional features optimizing code feel free to modify it :D .
        // see LICENCE arguments.

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

    private void sortListeners() {

        registeredListeners.sort(Comparator.comparingInt(this::getPriority).reversed());
    }

    private int getPriority(@NotNull Listener<?> listener) {
        EventHandler annotation = listener.getClass().getAnnotation(EventHandler.class);
        return (annotation != null) ? annotation.priority().getPriority() : Priority.NORMAL.getPriority();
    }

    public List<Listener<?>> getRegisteredListeners() {
        return registeredListeners;
    }
}
