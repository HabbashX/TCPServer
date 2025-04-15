package com.habbashx.tcpserver.event.handler;

import com.habbashx.tcpserver.event.Priority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {

    Priority priority() default Priority.LOW;

    boolean isAsync() default false;

}
