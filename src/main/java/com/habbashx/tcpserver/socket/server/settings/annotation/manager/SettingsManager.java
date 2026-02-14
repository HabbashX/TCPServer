package com.habbashx.tcpserver.socket.server.settings.annotation.manager;

import com.habbashx.tcpserver.socket.server.foundation.ServerFoundation;
import com.habbashx.tcpserver.socket.server.settings.annotation.Settings;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code SettingsManager} class is responsible for managing and initializing
 * server configuration settings by extracting values from the {@link Settings} annotation
 * present on the {@link ServerFoundation} class. It provides accessors for various
 * server properties such as host, port, security credentials, database connection details,
 * and chat settings. The {@link #initSettings()} method applies these settings to the
 * server's configuration.
 *
 * <p>
 * Usage:
 * <ul>
 *   <li>Instantiate with a {@link ServerFoundation} object.</li>
 *   <li>Access configuration values via getter methods.</li>
 *   <li>Call {@link #initSettings()} to apply settings to the server.</li>
 * </ul>
 * </p>
 */
public class SettingsManager {

    private final String host;

    /**
     * The port number on which the server will listen for incoming connections.
     * This property is injected using the @InjectProperty annotation and must be
     * configured properly to ensure the availability of server services.
     */
    private final int port;

    /**
     * Indicates whether the server can reuse the same address for creating new connections.
     * This property is typically used to configure the server socket binding behavior.
     * Injected from the configuration property named "reusableAddress".
     */
    private final boolean isReusableAddress;

    /**
     * Represents the file system path to the keystore containing security keys
     * and certificates used for secure communication or cryptographic operations.
     * This variable is injected with the property value associated with
     * the key "security.keystore.path" from the application's configuration.
     */
    private final String keyStorePath;

    /**
     * Represents the password for the security keystore.
     * This value is typically injected from a property source
     * using the key "security.keystore.password".
     * It is used to unlock the keystore for accessing cryptographic keys.
     */
    private final String keyStorePassword;

    /**
     * The file path to the trust store used for SSL/TLS configurations.
     * This property is injected with the value mapped to the configuration key
     * "security.truststore.path".
     */
    private final String trustStorePath;

    /**
     * Represents the password used to access the truststore.
     * This property is injected from the configuration using the key "security.truststore.password".
     * The truststore is typically used to store certificates for establishing secure communications.
     */
    private final String trustStorePassword;

    /**
     * Specifies the cooldown period for user chat interactions in seconds.
     * This value is typically used to limit the frequency at which users can send messages
     * to prevent spam or overuse of the chat functionality.
     * <p>
     * The value is injected from a configuration property "chat.cooldown.second".
     */
    private final String userChatCooldown;

    /**
     * Specifies the type of storage used for authentication data.
     * The value is injected from the configuration property "security.authentication.datastorage.type".
     */
    private final String authStorageType;

    /**
     * Represents the URL of the database used for establishing a connection to the database.
     * The value is injected through the configuration property "database.url".
     */
    private final String databaseURL;

    /**
     * Represents the username used to connect to the database.
     * This field is configured through the "database.username" property.
     * It is typically injected from an external configuration.
     */
    private final String databaseUsername;

    /**
     * The password used to authenticate the connection to the database.
     * This value is injected from the configuration property "database.password".
     */
    private final String databasePassword;

    private final ServerFoundation serverFoundation;

    public SettingsManager(@NotNull ServerFoundation serverFoundation) {
        this.serverFoundation = serverFoundation;
        Settings settings = serverFoundation.getClass().getAnnotation(Settings.class);

        host = settings.host();
        port = settings.port();
        isReusableAddress = settings.reusableAddress();
        keyStorePath = settings.keyStorePath();
        keyStorePassword = settings.keyStorePassword();
        trustStorePath = settings.trustStorePath();
        trustStorePassword = settings.trustStorePassword();
        userChatCooldown = settings.userChatCoolDown();
        authStorageType = String.valueOf(settings.type());
        databaseURL = settings.databaseURL();
        databaseUsername = settings.databaseUsername();
        databasePassword = settings.databasePassword();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isReusableAddress() {
        return isReusableAddress;
    }

    public String getKeystorePassword() {
        return keyStorePassword;
    }

    public String getKeystorePath() {
        return keyStorePath;
    }

    public String getTruststorePath() {
        return trustStorePath;
    }

    public String getTruststorePassword() {
        return trustStorePassword;
    }

    public String getUserChatCooldown() {
        return userChatCooldown;
    }

    public String getAuthStorageType() {
        return authStorageType;
    }

    public String getDatabaseURL() {
        return databaseURL;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void initSettings() {

        serverFoundation.getServerSettings().setHost(getHost());
        serverFoundation.getServerSettings().setPort(getPort());
        serverFoundation.getServerSettings().setReusableAddress(isReusableAddress());
        serverFoundation.getServerSettings().setKeyStorePath(getKeystorePath());
        serverFoundation.getServerSettings().setKeyStorePassword(getKeystorePassword());
        serverFoundation.getServerSettings().setTrustStorePath(getTruststorePath());
        serverFoundation.getServerSettings().setTrustStorePassword(getTruststorePassword());
        serverFoundation.getServerSettings().setUserChatCooldown(getUserChatCooldown());
        serverFoundation.getServerSettings().setAuthStorageType(getAuthStorageType());
        serverFoundation.getServerSettings().setDatabaseURL(getDatabaseURL());
        serverFoundation.getServerSettings().setDatabaseUsername(getDatabaseUsername());
        serverFoundation.getServerSettings().setDatabasePassword(getDatabasePassword());
    }

}
