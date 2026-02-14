package com.habbashx.tcpserver.command.defaultcommand;

import com.habbashx.tcpserver.annotation.MaybeEmpty;
import com.habbashx.tcpserver.command.Command;
import com.habbashx.tcpserver.command.CommandContext;
import com.habbashx.tcpserver.command.CommandExecutor;
import com.habbashx.tcpserver.connection.UserHandler;
import com.habbashx.tcpserver.cooldown.TimeUnit;
import com.habbashx.tcpserver.event.PrivateGroupChatEvent;
import com.habbashx.tcpserver.socket.server.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.habbashx.tcpserver.logger.ConsoleColor.*;
import static com.habbashx.tcpserver.socket.server.Server.PrivateGroup;

/**
 * Represents a command for managing private groups.
 * <p>
 * This class extends the `CommandExecutor` and provides functionality
 * for handling group-related commands such as creating, deleting, joining,
 * leaving, and sending messages to private groups.
 * </p>
 */
@Command(
        name = "group",
        cooldownTimeUnit = TimeUnit.SECONDS,
        cooldownTime = 2L
)
public final class PrivateGroupCommand extends CommandExecutor {

    private static final String CHOOSE_COMMAND_MESSAGE = """
            %sPlease choose command ->
            %sCREATE create new group
            %sDELETE <id> delete the group by defining the id of the group,
            %sJOIN <id> join the group by defining the id of the group
            %sLEAVE leave the group
            %sSEND <message> sending message to the users in group
            %sGROUPS <created groups>%s
            %sINFO get the info of group like id and current users count in group
            """.formatted(RED, LIME_GREEN, BRIGHT_RED, BRIGHT_BLUE, BRIGHT_RED, ORANGE, PURPLE, BRIGHT_YELLOW, WHITE);

    private static final String CONSOLE_EXECUTE_COMMAND_WARNING_MESSAGE = RED + "this command cannot be executed by console" + WHITE;

    /**
     * Manages private group operations such as creating, deleting, joining,
     * and leaving groups, as well as sending messages and retrieving group information.
     */
    private final PrivateGroupManager groupManager;

    public PrivateGroupCommand(Server server) {
        this.groupManager = new PrivateGroupManager(server);
    }

    @Override
    public void execute(@NotNull CommandContext commandContext) {

        if (commandContext.getSender() instanceof final UserHandler userHandler) {

            if (commandContext.getArgs().isEmpty()) {
                userHandler.sendMessage(CHOOSE_COMMAND_MESSAGE);
                return;
            }
            final String command = commandContext.getArgs().get(0);
            if (command != null) {
                doCommand(command.toUpperCase(), userHandler, commandContext.getArgs());
            } else {
                userHandler.sendMessage(CHOOSE_COMMAND_MESSAGE);
            }
        } else {
            System.out.println(CONSOLE_EXECUTE_COMMAND_WARNING_MESSAGE);
        }
    }

    private void doCommand(@NotNull String command, @NotNull UserHandler userHandler, List<String> args) {

        switch (command) {
            case "CREATE" -> {
                groupManager.createNewGroup(userHandler);
            }
            case "DELETE" -> {
                groupManager.deleteGroup(userHandler);
            }
            case "JOIN" -> {
                groupManager.joinGroup(userHandler, args.get(1)); // args (1) is the group id
            }
            case "LEAVE" -> {
                groupManager.leaveGroup(userHandler);
            }
            case "SEND" -> {
                @MaybeEmpty
                @Nullable final String message = String.join(" ", args.subList(1, args.size()));
                groupManager.sendMessage(userHandler, message);
            }
            case "INFO" -> {
                groupManager.groupInfo(userHandler);
            }
            default -> System.out.println(CHOOSE_COMMAND_MESSAGE);
        }
    }

    /**
     * Manages private groups within the server.
     * <p>
     * This class provides methods to create, delete, join, leave, and send messages
     * within private groups. It also includes utility methods for displaying group
     * information in a tabular format.
     */
    static final class PrivateGroupManager {

        private final Server server;

        /**
         * Constructs a new PrivateGroupManager instance.
         *
         * @param server the server instance associated with this manager
         */
        public PrivateGroupManager(Server server) {
            this.server = server;
        }

        /**
         * Creates a new private group for the specified user.
         * <p>
         * If the user is already in a group, an error message is sent.
         *
         * @param userHandler the user for whom the group is to be created
         */
        public void createNewGroup(UserHandler userHandler) {

            @Nullable final PrivateGroup privateGroup = server.getPrivateGroups().get(userHandler);

            if (privateGroup == null) {
                server.getPrivateGroups().put(userHandler, new PrivateGroup(server, userHandler));
                server.getPrivateGroups().get(userHandler).addUser(userHandler.getUserDetails().getUsername());
                userHandler.sendMessage(LIME_GREEN + "group created successfully." + WHITE);
                return;
            }

            userHandler.sendMessage(BRIGHT_RED + "you`re already in group" + WHITE);
        }

        /**
         * Deletes the private group associated with the specified user.
         * <p>
         * If the user is not in a group, no action is taken.
         *
         * @param userHandler the user whose group is to be deleted
         */
        public void deleteGroup(UserHandler userHandler) {

            @Nullable final PrivateGroup privateGroup = server.getPrivateGroups().get(userHandler);

            if (isUserInGroup(userHandler)) {

                if (privateGroup != null) {
                    server.getPrivateGroups().remove(userHandler);
                    userHandler.sendMessage(LIME_GREEN + "group deleted successfully." + WHITE);
                    userHandler.sendMessage(server.getPrivateGroups().toString());
                    return;
                }
            } else {
                userHandler.sendMessage(BRIGHT_RED + "you`re not in group to delete it" + WHITE);
                return;
            }
            userHandler.sendMessage(BRIGHT_RED + "you`re not the owner of the group to delete it" + WHITE);
        }

        /**
         * Allows a user to join a private group by its unique identifier.
         * <p>
         * If the group ID matches an existing group, the user is added to the group.
         *
         * @param userHandler the user attempting to join the group
         * @param groupID     the unique identifier of the group to join
         */
        public void joinGroup(UserHandler userHandler, String groupID) {

            if (isUserInGroup(userHandler)) {
                userHandler.sendMessage(BRIGHT_RED + "you're already in group" + WHITE);
                return;
            }

            if (groupID == null || groupID.isEmpty()) {
                userHandler.sendMessage(BRIGHT_RED + "please enter group id" + WHITE);
                return;
            }

            server.getPrivateGroups().values().stream()
                    .filter(g -> groupID.equals(g.getGroupID()))
                    .findFirst()
                    .ifPresentOrElse(group -> {
                        if (group.getUsers().contains(userHandler)) {
                            userHandler.sendMessage(BRIGHT_YELLOW + "you're already in that specific group" + WHITE);
                        } else {
                            group.getUsers().add(userHandler);
                            final String username = userHandler.getUserDetails().getUsername();
                            group.broadcast(username + " joined the group");
                        }
                    }, () -> userHandler.sendMessage(BRIGHT_RED + "group with id " + groupID + " not found" + WHITE));

        }

        /**
         * Removes a user from their current private group.
         * <p>
         * If the user is not part of any group, no action is taken.
         *
         * @param userHandler the user attempting to leave the group
         */
        public void leaveGroup(UserHandler userHandler) {

            server.getPrivateGroups().values().stream()
                    .filter(g -> g.getUsers().contains(userHandler))
                    .findFirst()
                    .ifPresentOrElse(group -> {

                        if (group.getOwnerName().equals(userHandler.getUserDetails().getUsername())) {
                            userHandler.sendMessage(BRIGHT_RED + "you`re the owner of the group, you cannot leave it. you can delete it instead." + WHITE);
                            return;
                        }
                        group.getUsers().remove(userHandler);
                        final String username = userHandler.getUserDetails().getUsername();
                        group.broadcast(username + " left the group");
                        userHandler.sendMessage("you`ve left the group");
                    }, () -> userHandler.sendMessage(BRIGHT_RED + "you`re not in group to leave it" + WHITE));

        }

        /**
         * Sends a message to all members of the group the user belongs to.
         * <p>
         * If the user is not part of any group, no message is sent.
         *
         * @param userHandler the user sending the message
         * @param message     the message to be broadcast to the group
         */
        public void sendMessage(UserHandler userHandler, String message) {

            server.getPrivateGroups().values().stream()
                    .filter(g -> g.getUsers().contains(userHandler))
                    .findFirst()
                    .ifPresentOrElse(group -> server.getEventManager().triggerEvent(new PrivateGroupChatEvent(message, userHandler, group)),
                            () -> userHandler.sendMessage(BRIGHT_RED + "you`re not in group to send message" + WHITE));
        }

        /**
         * Displays information about the group the user belongs to.
         * <p>
         * This method is currently not implemented.
         *
         * @param userHandler the user requesting group information
         */
        public void groupInfo(UserHandler userHandler) {
            server.getPrivateGroups().values().stream()
                    .filter(g -> g.getUsers().contains(userHandler))
                    .findFirst()
                    .ifPresentOrElse(group -> userHandler.sendMessage("""
                                    Group ID: %s
                                    Group Owner: %s
                                    Current Users Count: %s
                                    """.formatted(group.getGroupID(), group.getOwnerName(), String.valueOf(group.getUsers().size()))),
                            () -> userHandler.sendMessage(BRIGHT_RED + "you`re not in group to get group info" + WHITE));
        }

        private boolean isUserInGroup(UserHandler userHandler) {

            return server.getPrivateGroups().values().stream()
                    .anyMatch(group -> group.getUsers().contains(userHandler));
        }

        public void printTable(@NotNull List<String[]> rows) {

            int[] columnWidth = calculateColumnWidth(rows);

            for (int i = 0; i < rows.size(); i++) {
                if (i == 0) {
                    printLine(columnWidth);
                }
                printRow(rows.get(i), columnWidth);
                printLine(columnWidth);
            }
        }

        private void printRow(String @NotNull [] row, int[] columnWidth) {
            System.out.print("|");

            for (int i = 0; i < row.length; i++) {
                System.out.print(" " + row[i]);

                int padding = columnWidth[i] - row[i].length();

                for (int j = 0; j < padding; j++) {
                    System.out.print(" ");
                }
                System.out.print(" |");
            }
            System.out.println();
        }

        private void printLine(int @NotNull [] columnWidth) {
            System.out.print("+");

            for (int width : columnWidth) {
                for (int i = 0; i < width + 2; i++) {
                    System.out.print("-");
                }
                System.out.print("+");
            }
            System.out.println();
        }

        private int @NotNull [] calculateColumnWidth(@NotNull List<String[]> rows) {

            int columns = rows.get(0).length;
            int[] columnsWidths = new int[columns];

            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {

                    columnsWidths[i] = Math.max(columnsWidths[i], row[i].length());
                }
            }
            return columnsWidths;
        }
    }
}

