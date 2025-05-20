package com.habbashx.tcpserver.command;

import java.util.List;

/**
 * Represents a context for a command execution, encapsulating the necessary details
 * about the command's invocation and sender.
 *
 * This class provides immutable details about the command's sender, arguments,
 * and the instance of the sender. It is commonly passed as a parameter to command execution methods.
 */
public final class CommandContext {

    private final String senderName;
    private final List<String> args;
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

