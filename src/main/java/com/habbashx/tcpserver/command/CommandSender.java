package com.habbashx.tcpserver.command;

import com.habbashx.tcpserver.command.exception.InvalidCommandSenderException;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.handler.console.ServerConsoleHandler;

import java.util.concurrent.locks.ReentrantLock;

public sealed abstract class CommandSender permits UserHandler, ServerConsoleHandler {

    private final ReentrantLock reentrantLock = new ReentrantLock();

    public abstract boolean isConsole();

    public void printMessage(String message) {

        if (this instanceof UserHandler userHandler) {
            userHandler.sendMessage(message);
        } else if (this instanceof ServerConsoleHandler) {
            System.out.println(message);
        } else {
            throw new InvalidCommandSenderException("invalid command sender: "+this);
        }
    }

    public ReentrantLock getReentrantLock() {
        return reentrantLock;
    }
}
