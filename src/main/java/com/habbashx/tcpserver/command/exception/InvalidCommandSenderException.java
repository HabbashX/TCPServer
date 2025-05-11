package com.habbashx.tcpserver.command.exception;

public class InvalidCommandSenderException extends RuntimeException {

    public InvalidCommandSenderException(String message) {
        super(message);
    }
}
