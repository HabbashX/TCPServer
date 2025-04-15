package com.habbashx.tcpserver.command;

import com.habbashx.tcpserver.cooldown.TimeUnit;
import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.habbashx.tcpserver.security.Permission.NO_PERMISSION_REQUIRED;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String name();

    int permission() default NO_PERMISSION_REQUIRED;

    String[] aliases () default "";

    String description() default "";

    boolean isAsync() default false;

    long cooldownTime() default 0L;

    @MagicConstant(flagsFromClass = TimeUnit.class)
    int cooldownTimeUnit() default 0;

    boolean executionLog() default false;

    String note() default "";

    String configFile() default "";
}
