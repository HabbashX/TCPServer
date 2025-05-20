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

    Priority priority() default Priority.LOW;

    long delayMilliSeconds();

}
