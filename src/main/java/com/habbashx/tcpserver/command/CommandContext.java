package com.habbashx.tcpserver.command;

import java.util.List;

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

