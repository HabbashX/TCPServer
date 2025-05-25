package com.habbashx.tcpserver.delayevent.handler;

import com.habbashx.tcpserver.event.Priority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a delay event handler. This annotation is
 * used to configure a listener's priority and delay when processing delay-based events.
 *
 * The handler is identified and registered during event management operations to ensure
 * that delay events are triggered following the specified delay and priority.
 *
 * Attributes:
 * - {@code priority}: Configures the priority of the annotated handler. Higher priority
 *   handlers are executed earlier. Defaults to {@code Priority.LOW}.
 * - {@code delayMilliSeconds}: Specifies the delay in milliseconds before the
 *   corresponding event is triggered. This value must be provided by the user
 *   to control the execution timing of the handler.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DelayEventHandler {

    /**
     * Defines the priority for a delay event handler. This priority determines
     * the order in which event handlers are executed, with higher priority handlers
     * being executed earlier than those with lower priority.
     *
     * @return the priority level of the event handler. Defaults to {@code Priority.LOW}.
     */
    Priority priority() default Priority.LOW;

    /**
     * Specifies the delay in milliseconds before an event handler is triggered.
     *
     * This method is part of the {@code @DelayEventHandler} annotation and is intended
     * to configure the delay timing for events processed by the annotated handler.
     * The delay ensures that the event is not executed immediately but rather
     * after the specified time has elapsed.
     *
     * @return the delay duration in milliseconds as a {@code long} value. The value
     *         must be explicitly defined when applying the {@code @DelayEventHandler}
     *         annotation to an event handler.
     */
    long delayMilliSeconds();

}
