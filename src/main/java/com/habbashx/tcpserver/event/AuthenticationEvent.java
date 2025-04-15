package com.habbashx.tcpserver.event;

import com.habbashx.tcpserver.handler.UserHandler;

public class AuthenticationEvent extends Event {

    private final UserHandler userHandler;
    private final boolean authenticated;
    private final boolean isRegisterOperation;

    public AuthenticationEvent(UserHandler userHandler,boolean authenticated,boolean isRegisterOperation) {
        super("AuthenticationEvent");
        this.userHandler = userHandler;
        this.authenticated = authenticated;
        this.isRegisterOperation = isRegisterOperation;
    }

    public UserHandler getUserHandler() {
        return userHandler;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean isRegisterOperation() {
        return isRegisterOperation;
    }

    @Override
    public boolean isCancelled() {
        return super.isCancelled();
    }
}
