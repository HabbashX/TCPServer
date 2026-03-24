package com.habbashx.tcpserver.exception;

import java.io.Serial;

public class UnknownPacketException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6285023957278311597L;

    public UnknownPacketException(String message) {
        super(message);
    }
}
