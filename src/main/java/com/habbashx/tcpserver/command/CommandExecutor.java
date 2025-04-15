package com.habbashx.tcpserver.command;

import com.habbashx.tcpserver.command.configuration.JsonConfiguration;
import com.habbashx.tcpserver.cooldown.CooldownManager;
import com.habbashx.tcpserver.socket.Server;

public abstract class CommandExecutor {

    private final CooldownManager cooldownManager = new CooldownManager();

    public JsonConfiguration loadConfiguration(Server server) {

        String configFile = getConfigFile();

        return new JsonConfiguration(configFile,server);
    }

    private String getConfigFile() {
        return this.getClass().getAnnotation(Command.class).configFile();
    }

   public abstract void execute(CommandContext commandContext);

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
}
