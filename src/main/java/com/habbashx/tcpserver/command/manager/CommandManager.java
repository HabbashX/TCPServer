package com.habbashx.tcpserver.command.manager;

import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.event.UserExecuteCommandEvent;
import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.handler.UserHandler;

import com.habbashx.tcpserver.security.RequiredPermission;
import com.habbashx.tcpserver.socket.Server;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

public final class CommandManager {

    private static final String NO_PERMISSION_MESSAGE = RED+"you don`t have permission to execute this command."+RESET;
    private static final String ERROR_IN_EXECUTING_MESSAGE = RED+"Error in executing"+RESET;
    private static final String UNKNOWN_COMMAND_MESSAGE = "unknown command try /help.";
    private static final String ON_COOLDOWN_MESSAGE = RED + "you`re on cooldown for %s %s"+RESET;

    private final Map<String, CommandExecutor> executors = Collections.synchronizedMap(new HashMap<>());

    private final Server server;

    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();


    public CommandManager(Server server){
        this.server = server;
    }

    public void registerCommand(String commandName ,CommandExecutor commandExecutor) {
        executors.put(commandName,commandExecutor);
    }

    public void registerCommand(@NotNull CommandExecutor commandExecutor) {

        final Class<? extends CommandExecutor> commandExecutorClass = commandExecutor.getClass();

        if (commandExecutorClass.isAnnotationPresent(Command.class)) {
            final Command commandInformation = commandExecutorClass.getAnnotation(Command.class);
            final String commandName = commandInformation.name();
            registerCommand(commandName,commandExecutor);

            if (0 < commandInformation.aliases().length) {
                for (final String alias : commandInformation.aliases()) {
                    registerCommand(alias,commandExecutor);
                }
            }
        } else {
            final String command = commandExecutorClass.getName();
            server.getServerLogger().error("""
                %s is missing the @Command annotation.
                Please ensure that the command class is annotated with @Command and includes the 'name' and optional 'aliases' attributes.
                The command %s will not be executed until properly annotated.
                """.formatted(command, command));

        }
    }

    @SuppressWarnings("deprecation")
    public void executeCommand(@NotNull String senderName , @NotNull String message , @NotNull CommandSender commandSender) {
        // Warning do not modify any line of code in this method,
        // as only if you understand what`s going on right here ok ?,
        // if you want to add anything do you want additional features optimizing code feel free to modify it :D .
        // see LICENCE arguments.

        if (!message.startsWith("/")) return;

        @Language(value = "RegExp") final String[] parts = message.substring(1).split(" ");
        final String commandName = parts[0].toLowerCase();
        final List<String> args = List.of(parts).subList(1, parts.length);

        final CommandExecutor commandExecutor = executors.get(commandName);

        if (commandExecutor != null) {
            final Class<? extends CommandExecutor> commandExecutorClass = commandExecutor.getClass();

            if (commandExecutorClass.isAnnotationPresent(Command.class)) {

                final int permissionValue = commandExecutorClass.isAnnotationPresent(Command.class) ? commandExecutorClass.getAnnotation(Command.class).permission() : commandExecutorClass.getAnnotation(RequiredPermission.class).value();
                final Command commandInformation = commandExecutorClass.getAnnotation(Command.class);
                long cooldownTime = getCooldownTimeUnit(commandInformation.cooldownTime(),commandInformation.cooldownTimeUnit());
                commandExecutor.getCooldownManager().setCooldownTime(cooldownTime);
                if (commandSender instanceof UserHandler userHandler) {


                    if (permissionValue == 0X00 || userHandler.hasPermission(permissionValue) || userHandler.hasPermission(0X0EFA)) {

                        if (!commandExecutor.getCooldownManager().isOnCooldown(senderName)) {
                            CommandContext commandContext = new CommandContext(senderName, args, userHandler);
                            executeCommand(commandExecutor,commandContext,commandInformation.isAsync());
                            if (commandInformation.executionLog()) {
                                server.getEventManager().triggerEvent(new UserExecuteCommandEvent(senderName, userHandler, commandExecutor));
                            }
                            commandExecutor.getCooldownManager().setCooldown(senderName);
                        } else {
                            final int cooldown = (int) commandExecutor.getCooldownManager().getRemainingTime(senderName);
                            if (commandInformation.cooldownTimeUnit() == TimeUnit.SECONDS) {
                                userHandler.sendMessage(ON_COOLDOWN_MESSAGE.formatted(cooldown,"seconds"));
                            } else if (commandInformation.cooldownTimeUnit() == TimeUnit.MILLI_SECONDS) {
                                userHandler.sendMessage(ON_COOLDOWN_MESSAGE.formatted(cooldown,"milli seconds"));
                            }
                        }
                    } else {
                        userHandler.sendMessage(NO_PERMISSION_MESSAGE);
                    }
                } else {
                    CommandContext commandContext = new CommandContext(senderName,args, commandSender);
                    executeCommand(commandExecutor,commandContext,commandInformation.isAsync());
                }
            } else {
                sendMessage(commandSender,ERROR_IN_EXECUTING_MESSAGE);
            }
        } else {
            sendMessage(commandSender,UNKNOWN_COMMAND_MESSAGE);
        }
    }

    private void executeCommand(CommandExecutor commandExecutor,CommandContext commandContext, boolean isAsync) {

        if (isAsync) {
            asyncExecutor.submit(() -> commandExecutor.execute(commandContext));
        } else {
            commandExecutor.execute(commandContext);
        }
    }
    private void sendMessage(CommandSender commandSender, String message){

        if (commandSender instanceof UserHandler userHandler) {
            userHandler.sendMessage(message);
        } else {
            System.out.println(message);
        }
    }

    private long getCooldownTimeUnit(long cooldown ,int timeUnit) {
        if (timeUnit == TimeUnit.MILLI_SECONDS) {
            return cooldown / 1000;
        } else if (timeUnit == TimeUnit.SECONDS) {
            return cooldown;
        } else {
            server.getServerLogger().error("invalid time unit: "+timeUnit);
            return cooldown;
        }
    }
    public void disableAllCommands() {
        executors.clear();
    }

    public boolean disableCommand(String command) {

        final CommandExecutor executor = executors.get(command);

        if (executor != null){
            final Command commandInformation = executor.getClass().getAnnotation(Command.class);

            if (0 < commandInformation.aliases().length) {
                for (String alias : commandInformation.aliases()) {
                    executors.remove(alias);
                }
            }
            executors.remove(command);
            return true;
        } else {
            return false;
        }
    }


    public Map<String, CommandExecutor> getExecutors() {
        return executors;
    }

    public @NotNull List<String> getAllCommands() {

        final List<String> command = new ArrayList<>();
        for (final Map.Entry<String,CommandExecutor> commandExecutorEntry :executors.entrySet()) {
            command.add(commandExecutorEntry.getKey());
        }
        return command;
    }

    public @NotNull List<String> getAllExecutors() {

        final List<String> executorList = new ArrayList<>();
        for (final Map.Entry<String, CommandExecutor> commandExecutorEntry : executors.entrySet()) {
            executorList.add(commandExecutorEntry.getKey());
        }
        return executorList;
    }

}
