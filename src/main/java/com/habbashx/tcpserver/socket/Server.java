package com.habbashx.tcpserver.socket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habbashx.tcpserver.command.defaultcommand.BanCommand;
import com.habbashx.tcpserver.command.defaultcommand.ChangeRoleCommand;
import com.habbashx.tcpserver.command.defaultcommand.HelpCommand;
import com.habbashx.tcpserver.command.defaultcommand.InfoCommand;
import com.habbashx.tcpserver.command.defaultcommand.ListUserCommand;
import com.habbashx.tcpserver.command.defaultcommand.MuteCommand;
import com.habbashx.tcpserver.command.defaultcommand.NicknameCommand;
import com.habbashx.tcpserver.command.defaultcommand.PrivateMessageCommand;

import com.habbashx.tcpserver.command.defaultcommand.UnBanCommand;
import com.habbashx.tcpserver.command.defaultcommand.UnMuteCommand;
import com.habbashx.tcpserver.command.defaultcommand.UserDetailsCommand;
import com.habbashx.tcpserver.command.manager.BanCommandManager;
import com.habbashx.tcpserver.command.manager.CommandManager;

import com.habbashx.tcpserver.command.manager.MuteCommandManager;

import com.habbashx.tcpserver.delayevent.manager.DelayEventManager;
import com.habbashx.tcpserver.event.manager.EventManager;

import com.habbashx.tcpserver.handler.UserHandler;

import com.habbashx.tcpserver.handler.console.ServerConsoleHandler;

import com.habbashx.tcpserver.listener.handler.AuthenticationEventHandler;
import com.habbashx.tcpserver.listener.handler.DefaultBroadcastHandler;
import com.habbashx.tcpserver.listener.handler.DefaultChatHandler;
import com.habbashx.tcpserver.listener.handler.DefaultServerConsoleChatHandler;
import com.habbashx.tcpserver.listener.handler.DefaultUserExecuteCommandHandler;
import com.habbashx.tcpserver.listener.handler.DefaultUserJoinHandler;
import com.habbashx.tcpserver.listener.handler.DefaultUserLeaveHandler;
import com.habbashx.tcpserver.listener.handler.DefaultMutedUserHandler;

import com.habbashx.tcpserver.logger.ServerLogger;

import com.habbashx.tcpserver.security.AuthStorageType;
import com.habbashx.tcpserver.security.DefaultAuthentication;
import com.habbashx.tcpserver.security.Authentication;

import com.habbashx.tcpserver.security.Role;
import com.habbashx.tcpserver.settings.ServerSettings;
import com.habbashx.tcpserver.user.UserDetails;
import com.habbashx.tcpserver.version.VersionChecker;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public final class Server implements Runnable, Closeable {

    private SSLServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private final List<UserHandler> connections = Collections.synchronizedList(new ArrayList<>());

    private final EventManager eventManager = new EventManager(this);
    private final DelayEventManager delayEventManager = new DelayEventManager(this);
    private final CommandManager commandManager = new CommandManager(this);

    private final ServerLogger serverLogger = new ServerLogger();

    private final ServerSettings serverSettings = new ServerSettings();

    private final ServerDataManager serverDataManager = new ServerDataManager(this);

    private final MuteCommandManager muteCommandManager = new MuteCommandManager();
    private final BanCommandManager banCommandManager = new BanCommandManager();

    private Authentication authentication = new DefaultAuthentication(this);

    private boolean running = true;

    public Server() {
        registerDefaultEvents();
        registerDefaultDelayEvents();
        registerDefaultCommands();
        registerKeystore();
    }

    public void registerKeystore() {
        System.setProperty("javax.net.ssl.keyStore",serverSettings.getKeystorePath());
        System.setProperty("javax.net.ssl.keyStorePassword",serverSettings.getKeystorePassword());
    }

    @Override
    public void run() {

        try  {
            serverLogger.info("Server started at port: "+serverSettings.getPort());
            serverLogger.info("Server is secured with ssl protocol");
            serverLogger.info("waiting for user connections....");

            SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(serverSettings.getPort());

            ServerConsoleHandler serverConsoleHandler = new ServerConsoleHandler(this);
            threadPool.execute(serverConsoleHandler);
            VersionChecker.checkProjectVersion(this);
            while (running) {
                SSLSocket user = (SSLSocket) serverSocket.accept();
                user.setReuseAddress(serverSocket.getReuseAddress());
                UserHandler userHandler = new UserHandler(user,this);
                threadPool.execute(userHandler);
                connections.add(userHandler);
            }
        } catch (IOException e) {
            serverLogger.error(e.getMessage());
            shutdown();
        }

    }

    private void registerDefaultEvents() {
        eventManager.registerEvent(new DefaultChatHandler(this));
        eventManager.registerEvent(new DefaultMutedUserHandler(muteCommandManager));
        eventManager.registerEvent(new DefaultUserJoinHandler(this));
        eventManager.registerEvent(new DefaultUserLeaveHandler(this));
        eventManager.registerEvent(new DefaultServerConsoleChatHandler(this));
        eventManager.registerEvent(new AuthenticationEventHandler());
        eventManager.registerEvent(new DefaultUserExecuteCommandHandler(this));
    }

    private void registerDefaultDelayEvents() {
        delayEventManager.registerEvent(new DefaultBroadcastHandler());
    }

    private void registerDefaultCommands() {
        commandManager.registerCommand(new ChangeRoleCommand(this));
        commandManager.registerCommand(new HelpCommand(this));
        commandManager.registerCommand(new PrivateMessageCommand(this));
        commandManager.registerCommand(new ListUserCommand(this));
        commandManager.registerCommand(new BanCommand(this,banCommandManager));
        commandManager.registerCommand(new UnBanCommand(this,banCommandManager));
        commandManager.registerCommand(new MuteCommand(this ,muteCommandManager));
        commandManager.registerCommand(new UnMuteCommand(this,muteCommandManager));
        commandManager.registerCommand(new UserDetailsCommand(this));
        commandManager.registerCommand(new InfoCommand());
        commandManager.registerCommand(new NicknameCommand());

    }

    public void broadcast(String message) {
        for (final UserHandler user : getConnections()) {
            synchronized (this) {
                if (user != null) {
                    user.sendMessage(message);
                }
            }
        }
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public SSLServerSocket getServerSocket() {
        return serverSocket;
    }

    public List<UserHandler> getConnections() {
        return connections;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public ServerLogger getServerLogger() {
        return serverLogger;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public DelayEventManager getDelayEventManager() {
        return delayEventManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public BanCommandManager getBanCommandManager() {
        return banCommandManager;
    }

    public MuteCommandManager getMuteCommandManager() {
        return muteCommandManager;
    }

    public ServerSettings getServerSettings() {
        return serverSettings;
    }

    public ServerDataManager getServerDataManager() {
        return serverDataManager;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void shutdown() {
        try {

            running = false;
            if (serverSocket != null) {
                if (!serverSocket.isClosed()) {
                    serverSocket.close();
                }
            }

            if (!threadPool.isShutdown()) {
                threadPool.shutdown();
            }

            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
            connections.forEach(UserHandler::shutdown);

            serverLogger.warning("server shutdown");
        } catch (IOException  | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    @Deprecated()
    private void initializeShutdownHookOperation() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public void close() throws IOException {

        running = false;
        if (serverSocket != null) {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        }

        if (!threadPool.isShutdown()) {
            threadPool.shutdown();
        }

        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        connections.forEach(UserHandler::shutdown);
        serverLogger.warning("server is shutdown");
    }

    public static void main(String[] args) {
        try (Server server = new Server()) {
            server.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public static final class ServerDataManager {

        private final Server server;
        private final AuthStorageType authStorageType;
        private final UserDao userDao;

        public ServerDataManager(@NotNull Server server) {
            this.server = server;
            String authType = server.getServerSettings().getAuthStorageType().toUpperCase();
            authStorageType = AuthStorageType.valueOf(authType);
            userDao = new UserDao(server);
        }

        public @Nullable UserHandler getOnlineUserByUsername(String username) {
            return server.getConnections()
                    .stream()
                    .filter(e -> {
                        if (e.getUserDetails().getUsername() != null) {
                            return e.getUserDetails().getUsername().equals(username);
                        } else {
                            return false;
                        }

                    })
                    .findFirst()
                    .orElse(null);
        }

        public @Nullable UserHandler getOnlineUserById(String id) {
            return server.getConnections()
                    .stream()
                    .filter(e -> e.getUserDetails().getUserID().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        public @Nullable UserDetails getUserByUsername(String username) {
            return getUserDetails("username",username);
        }

        public @Nullable UserDetails getUserById(String id) {
            return getUserDetails("userID",id);
        }

        public @Nullable UserDetails getUserByEmail(String email) {
            return getUserDetails("userEmail",email);
        }

        private @Nullable UserDetails getUserDetails(String element, String specific) {

            try {
                return switch (authStorageType) {
                    case CSV -> getUserDetailsFromCsvFile(element, specific);
                    case JSON -> getUserDetailsFromJsonFile(element, specific);
                    case SQL -> getUserDetailsFromDatabase(element, specific);
                };
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private @Nullable UserDetails getUserDetailsFromCsvFile(String element, String specific) {
            try (final Reader reader = new FileReader("data/users.csv")) {
                Iterable<CSVRecord> userIterable = CSVFormat.DEFAULT.parse(reader);

                for (CSVRecord record : userIterable) {
                    if (record.get(element).equals(specific)) {
                        return new UserDetails().builder()
                                .userIP(record.get("userIP"))
                                .userID(record.get("userID"))
                                .userRole(Role.valueOf(record.get("userRole")))
                                .username(record.get("username"))
                                .userEmail(record.get("userEmail"))
                                .phoneNumber(record.get("phoneNumber"))
                                .activeAccount(Boolean.parseBoolean(record.get("isActiveAccount")))
                                .build();
                    }
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private @Nullable UserDetails getUserDetailsFromJsonFile(String element , String specific) {
            final ObjectMapper mapper = new ObjectMapper();

            try {
                List<Map<String,Object>> users = mapper.readValue(new File("data/users.json"), new TypeReference<>() {});

                if (users != null) {
                    for (Map<String, Object> user : users) {

                        if (user != null) {
                            if (user.get(element).equals(specific)) {

                                return new UserDetails().builder()
                                        .userIP((String) user.get("userIP"))
                                        .userID((String) user.get("userID"))
                                        .userRole(Role.valueOf((String) user.get("userRole")))
                                        .userEmail((String) user.get("userEmail"))
                                        .username((String) user.get(("username")))
                                        .phoneNumber((String) user.get("phoneNumber"))
                                        .activeAccount((boolean) user.get("isActiveAccount"))
                                        .build();
                            }
                        }
                    }
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        private @Nullable UserDetails getUserDetailsFromDatabase(String column,String specific) throws SQLException {
            return userDao.getUser(column,specific);
        }

        public UserDao getUserDao() {
            return userDao;
        }
    }

    public static final class UserDao {

        private final Server server;

        private final Connection connection;
        @Contract(pure = true)
        public UserDao(@NotNull Server server) {
          this.server = server;
          try {
              connection = getConnection();
          } catch (SQLException e) {
              throw new RuntimeException(e);
          }
        }

        private Connection getConnection() throws SQLException {
            final String url = server.getServerSettings().getDatabaseURL();
            final String username = server.getServerSettings().getDatabaseUsername();
            final String password = server.getServerSettings().getDatabasePassword();
            return DriverManager.getConnection(url, username,password);
        }

        public void insertUser(String username,
                               String password,
                               String userID,
                               String userIP,
                               String userRole,
                               String userEmail,
                               String phoneNumber,
                               boolean isActiveAccount) throws SQLException {

            final String sql = "INSERT INTO users (userID, userIP, userRole, username, password, userEmail, phoneNumber, isActiveAccount) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, userID);
                stmt.setString(2, userIP);
                stmt.setString(3, userRole);
                stmt.setString(4, username);
                stmt.setString(5,password);
                stmt.setString(6,userEmail);
                stmt.setString(7,phoneNumber);
                stmt.setBoolean(8,isActiveAccount);
                stmt.executeUpdate();
            }
        }

        public void updateUser(String column,String targetColumn,String specific,String newValue) throws SQLException {
            final String sql = "UPDATE users SET %s = ? WHERE %s = ?".formatted(targetColumn,column);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newValue);
                stmt.setString(2, specific);
                stmt.executeUpdate();
            }
        }

        public void deleteUser(String element , String specific) throws SQLException {
            final String sql = "DELETE FROM users WHERE %s = ?".formatted(element);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, specific);
                stmt.executeUpdate();
            }
        }

        public @Nullable UserDetails getUser(String column,String specific) throws SQLException {
            final String sql = "SELECT * FROM users WHERE %s = ?".formatted(column);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1,specific);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    UserDetails user = new UserDetails();
                    user.setUserID(rs.getString("userID"));
                    user.setUserIP(rs.getString("userIP"));
                    user.setUserRole(Role.valueOf(rs.getString("userRole")));
                    user.setUsername(rs.getString("username"));
                    user.setUserEmail(rs.getString("userEmail"));
                    user.setPhoneNumber(rs.getString("phoneNumber"));
                    user.setActiveAccount(rs.getBoolean("isActiveAccount"));
                    return user;
                }
            }
            return null;
        }

        public @Nullable String getHashedPassword(String username) throws SQLException{
            final String sql = "SELECT * FROM users WHERE username = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1,username);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
            return null;
        }
    }
}


