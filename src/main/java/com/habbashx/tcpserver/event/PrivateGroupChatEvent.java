package com.habbashx.tcpserver.event;

import com.habbashx.tcpserver.connection.UserHandler;

import static com.habbashx.tcpserver.socket.server.Server.PrivateGroup;

public class PrivateGroupChatEvent extends Event {


    private final UserHandler userHandler;
    private final String message;
    private final PrivateGroup privateGroup;

    public PrivateGroupChatEvent(String message, UserHandler userHandler, PrivateGroup privateGroup) {
        super("PrivateGroupChatEvent");
        this.userHandler = userHandler;
        this.message = message;
        this.privateGroup = privateGroup;
    }


    public UserHandler getUserHandler() {
        return userHandler;
    }

    public String getMessage() {
        return message;
    }

    public PrivateGroup getPrivateGroup() {
        return privateGroup;
    }
}

