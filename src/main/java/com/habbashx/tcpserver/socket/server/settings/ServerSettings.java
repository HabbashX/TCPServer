package com.habbashx.tcpserver.socket.server.settings;

import com.habbashx.annotation.InjectProperty;

/**
 * The ServerSettings class encapsulates configuration properties required for server setup,
 * security settings, user management, and database connectivity.
 * <p>
 * Properties are injected via the {@code @InjectProperty} annotation, allowing external
 * configuration to be defined and automatically loaded. This class functions as a central
 * repository for the server's settings, supporting ease of access and maintainability.
 * <p>
 * The injected properties include:
 * - Network settings like port and address reuse preferences.
 * - SSL/TLS security configurations for keystore and truststore paths and passwords.
 * - User interaction settings such as chat cooldown duration.
 * - Authentication storage type for user management.
 * - Database configurations including URL, username, and password.
 * <p>
 * This class is immutable, ensuring thread safety for concurrent access in a multi-threaded
 * server environment. The provided getter methods allow controlled access to the configuration
 * values after they are injected during initialization.
 */
public final class ServerSettings {

    /**
     * The port number on which the server will listen for incoming connections.
     * This property is injected using the @InjectProperty annotation and must be
     * configured properly to ensure the availability of server services.
     */
    @InjectProperty("port")
    private int port;

    /**
     * Indicates whether the server can reuse the same address for creating new connections.
     * This property is typically used to configure the server socket binding behavior.
     * Injected from the configuration property named "reusableAddress".
     */
    @InjectProperty("reusableAddress")
    private boolean isReusableAddress;

    /**
     * Represents the file system path to the keystore containing security keys
     * and certificates used for secure communication or cryptographic operations.
     * This variable is injected with the property value associated with
     * the key "security.keystore.path" from the application's configuration.
     */
    @InjectProperty("security.keystore.path")
    private String keyStorePath;

    /**
     * Represents the password for the security keystore.
     * This value is typically injected from a property source
     * using the key "security.keystore.password".
     * It is used to unlock the keystore for accessing cryptographic keys.
     */
    @InjectProperty("security.keystore.password")
    private String keyStorePassword;

    /**
     * The file path to the trust store used for SSL/TLS configurations.
     * This property is injected with the value mapped to the configuration key
     * "security.truststore.path".
     */
    @InjectProperty("security.truststore.path")
    private String trustStorePath;

    /**
     * Represents the password used to access the truststore.
     * This property is injected from the configuration using the key "security.truststore.password".
     * The truststore is typically used to store certificates for establishing secure communications.
     */
    @InjectProperty("security.truststore.password")
    private String trustStorePassword;

    /**
     * Specifies the cooldown period for user chat interactions in seconds.
     * This value is typically used to limit the frequency at which users can send messages
     * to prevent spam or overuse of the chat functionality.
     * <p>
     * The value is injected from a configuration property "chat.cooldown.second".
     */
    @InjectProperty("chat.cooldown.second")
    private String userChatCooldown;

    /**
     * Specifies the type of storage used for authentication data.
     * The value is injected from the configuration property "security.authentication.datastorage.type".
     */
    @InjectProperty("security.authentication.datastorage.type")
    private String authStorageType;

    /**
     * Represents the URL of the database used for establishing a connection to the database.
     * The value is injected through the configuration property "database.url".
     */
    @InjectProperty("database.url")
    private String databaseURL;

    /**
     * Represents the username used to connect to the database.
     * This field is configured through the "database.username" property.
     * It is typically injected from an external configuration.
     */
    @InjectProperty("database.username")
    private String databaseUsername;

    /**
     * The password used to authenticate the connection to the database.
     * This value is injected from the configuration property "database.password".
     */
    @InjectProperty("database.password")
    private String databasePassword;

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
}
