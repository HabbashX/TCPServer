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

    Priority priority() default Priority.LOW;

    boolean isAsync() default false;

}
