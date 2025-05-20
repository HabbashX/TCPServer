package com.habbashx.tcpserver.command;

import com.habbashx.tcpserver.cooldown.TimeUnit;
import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.habbashx.tcpserver.security.Permission.NO_PERMISSION_REQUIRED;

/**
 * Annotation to define and configure metadata for a specific command. This annotation
 * can be applied to classes representing commands to specify their name, permissions,
 * aliases, description, and various execution-related configurations like cooldowns or
 * logging.
 */
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
