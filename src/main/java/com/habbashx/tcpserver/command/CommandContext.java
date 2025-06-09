package com.habbashx.tcpserver.command;

import java.util.List;

/**
 * Represents a context for a command execution, encapsulating the necessary details
 * about the command's invocation and sender.
 * <p>
 * This class provides immutable details about the command's sender, arguments,
 * and the instance of the sender. It is commonly passed as a parameter to command execution methods.
 */
public final class CommandContext {

    /**
     * The name of the sender who initiated the command execution.
     * <p>
     * This variable holds a string representing the identifier or name of the entity
     * responsible for invoking the command. The sender can be a user, console, or any
     * other entity identified by a name.
     * <p>
     * It is immutable and set during the instantiation of the containing class.
     */
    private final String senderName;
    /**
     * A list of command arguments provided during the execution of a command.
     * <p>
     * This immutable list stores strings representing individual arguments
     * passed to the command. The arguments are typically parsed from the command
     * input and may represent parameters, options, or flags influencing the
     * command's behavior.
     */
    private final List<String> args;
    /**
     * The commandSender represents the entity responsible for executing or initiating
     * a command in the context of the application. It provides the methods and context
     * needed to interact with the system or user that issued the command.
     * <p>
     * This variable is immutable and ensures thread-safe operations through the
     * underlying implementation of the CommandSender interface.
     * <p>
     * Examples of possible implementations include user-based senders or server
     * console-based senders.
     */
    private final CommandSender commandSender;

    public CommandContext(String senderName, List<String> args, CommandSender commandSender) {
        this.senderName = senderName;
        this.args = args;
        this.commandSender = commandSender;

    }

    public String getSenderName() {
        return senderName;
    }

    public List<String> getArgs() {
        return args;
    }

    public CommandSender getSender() {
        return commandSender;
    }
}

