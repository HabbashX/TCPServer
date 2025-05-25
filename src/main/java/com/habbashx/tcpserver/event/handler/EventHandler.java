package com.habbashx.tcpserver.event.handler;

import com.habbashx.tcpserver.event.Priority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code EventHandler} annotation is used to indicate that a class implementing
 * the {@code Listener<E>} interface will handle certain types of events. This annotation
 * is essential for the EventManager to recognize and register the corresponding listeners.
 * The annotation provides configurable properties to set the execution priority and
 * whether the event handling should occur asynchronously.
 *
 * The annotated listener's priority determines the order in which it will be invoked
 * relative to other listeners. The event handling can either execute synchronously or
 * asynchronously based on the {@code isAsync} option.
 *
 * Annotated listeners must be properly registered within the {@code EventManager}
 * for them to be invoked upon triggered events. If the annotation is missing,
 * the EventManager will skip registering the listener and may log a corresponding error.
 *
 * Properties:
 * - {@code priority}: Sets the priority level of the listener, influencing its invocation order.
 *   Defaults to {@code Priority.LOW}.
 * - {@code isAsync}: Specifies if the event handling is to execute asynchronously. Defaults to {@code false}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {

    /**
     * Specifies the priority level for the event listener annotated with {@code @EventHandler}.
     * The priority determines the order in which listeners will be executed. Listeners with
     * higher priority levels are executed earlier than those with lower levels.
     *
     * @return the priority level for the annotated listener. Defaults to {@code Priority.LOW}.
     */
    Priority priority() default Priority.LOW;

    /**
     * Determines whether the annotated event handler should execute asynchronously.
     *
     * Event handlers marked as asynchronous will be executed in a separate thread,
     * allowing non-blocking behavior and improved performance for operations
     * that may take a significant amount of time. If set to {@code false},
     * the event handler will execute synchronously, in the same thread in which
     * the event was triggered.
     *
     * By default, this property is set to {@code false}.
     *
     * @return {@code true} if the event handler should execute asynchronously,
     *         {@code false} otherwise
     */
    boolean isAsync() default false;

    /**
     * Specifies the path to the configuration file.
     *
     * @return the path to the configuration file as a string. Returns an empty string by default.
     */
    String configFile() default "";

}
