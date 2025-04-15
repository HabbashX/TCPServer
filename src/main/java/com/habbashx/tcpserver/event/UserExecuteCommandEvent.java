package com.habbashx.tcpserver.event;

import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.handler.UserHandler;

public class UserExecuteCommandEvent extends Event {

    private final String username;
    private final UserHandler user;
    private final CommandExecutor commandExecutor;

    public UserExecuteCommandEvent(String username , UserHandler user ,CommandExecutor commandExecutor) {
        super("UserExecuteCommandEvent");
        this.username = username;
        this.user = user;
        this.commandExecutor = commandExecutor;
    }

    public String getUsername() {
        return username;
    }

    public UserHandler getUser() {
        return user;
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        super.setCancelled(cancelled);
    }

    @Override
    public boolean isCancelled() {
        return super.isCancelled();
    }
}
