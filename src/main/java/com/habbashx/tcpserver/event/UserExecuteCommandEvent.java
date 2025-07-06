package com.habbashx.tcpserver.event;

import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.connection.UserHandler;

/**
 * Represents an event triggered when a user executes a command.
 * This event is used in the system to handle and process user command actions.
 * It provides information about the user executing the command, the command executor associated with it,
 * and the username of the user performing the action.
 * <br>
 * The event can be cancelled to prevent the command from being fully executed.
 */
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
