package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.event.UserExecuteCommandEvent;

import com.habbashx.tcpserver.listener.Listener;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;

@EventHandler(isAsync = true)
public final class DefaultUserExecuteCommandHandler implements Listener<UserExecuteCommandEvent> {

    private final Server server;

    public DefaultUserExecuteCommandHandler(Server server) {
        this.server = server;
    }

    @Override
    public void onEvent(@NotNull UserExecuteCommandEvent event) {
        server.getServerLogger().monitor("the user: "+event.getUsername() +" executed the command "+event.getCommandExecutor());
    }
}
