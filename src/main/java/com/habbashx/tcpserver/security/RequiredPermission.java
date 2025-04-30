package com.habbashx.tcpserver.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated(since = "1.0") // soon it will become as mark for removal
public @interface RequiredPermission {
    int value();
}
