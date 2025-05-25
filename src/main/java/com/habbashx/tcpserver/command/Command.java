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

    /**
     * Retrieves the name of the command. The name is a unique identifier
     * used to represent the command during execution, configuration, and logging.
     *
     * @return the name of the command as a non-null string.
     */
    String name();

    /**
     * Specifies the required permission level for executing the command. This value determines
     * the access control required to run the associated command. If not explicitly specified,
     * the default value is used, indicating no permission is required.
     *
     * @return the permission level as an integer. A value of NO_PERMISSION_REQUIRED indicates
     *         no special permissions are needed; other values may correspond to specific
     *         permission requirements as defined by the implementation.
     */
    int permission() default NO_PERMISSION_REQUIRED;

    /**
     * Retrieves the aliases associated with the command. Aliases are alternative
     * names or shortcuts that can be used to invoke the command.
     *
     * @return an array of strings representing the command's aliases. If no aliases
     *         are defined, an empty array is returned.
     */
    String[] aliases () default "";

    /**
     * Returns a description associated with the method or element where it is used.
     *
     * @return a string representing the description, or an empty string if no description is provided.
     */
    String description() default "";

    /**
     * Determines whether the command should be executed asynchronously.
     * When set to true, the command will run in a separate thread,
     * allowing the main thread to remain unblocked by potentially long-running tasks.
     *
     * @return true if the command is to be executed asynchronously;
     *         false if it should run synchronously by default.
     */
    boolean isAsync() default false;

    /**
     * Specifies the cooldown time in milliseconds.
     * The cooldown time is the duration that must elapse before a subsequent action or method call can occur.
     *
     * @return the cooldown time in milliseconds, defaulting to 0 if not specified
     */
    long cooldownTime() default 0L;

    /**
     * Specifies the unit of time used for configuring the cooldown duration of the command.
     * The unit must correspond to a constant defined in the {@link TimeUnit} class.
     *
     * @return an integer representing the time unit for cooldowns, such as {@link TimeUnit#MILLI_SECONDS}
     *         for milliseconds or {@link TimeUnit#SECONDS} for seconds. Defaults to 0.
     */
    @MagicConstant(flagsFromClass = TimeUnit.class)
    int cooldownTimeUnit() default 0;

    /**
     * Indicates whether an execution log should be generated for the associated command.
     * When set to true, details of the command execution are recorded in the log for
     * auditing or debugging purposes.
     *
     * @return true if an execution log is enabled; false otherwise.
     */
    boolean executionLog() default false;

    /**
     * Specifies an additional note or remark about the command.
     * This value can be used to provide supplementary information relevant to
     * the execution or behavior of the command.
     *
     * @return a string representing the note, or an empty string if no note is provided.
     */
    String note() default "";

    /**
     * Specifies the configuration file path associated with the command.
     * This value can be used to define a specific configuration file that
     * the command depends on or interacts with.
     *
     * @return the configuration file path as a string. Returns an empty string if
     *         no configuration file path is specified.
     */
    String configFile() default "";
}
