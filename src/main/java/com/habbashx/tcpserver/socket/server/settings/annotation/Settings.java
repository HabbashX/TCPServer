package com.habbashx.tcpserver.socket.server.settings.annotation;

import com.habbashx.tcpserver.security.auth.storage.AuthStorageType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation represents the settings for the server,
 * including host, port, keystore, truststore, and database configurations.
 * It is used to define the server's operational parameters and security settings.
 * The values specified in this annotation will be used to configure the server
 * at runtime.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Settings {

    /**
     * represent the name of settings file,
     * this file will be used to store the settings
     * for the server.
     */
    String host() default "127.0.0.1"; //localhost

    /**
     * represent the port of server,
     * this port will be used to listen for incoming connections.
     */
    int port() default 8080;

    /**
     * represent the value of reusable address,
     * this value will be used to determine whether
     * the server can reuse the same address for creating new connections.
     */
    boolean reusableAddress() default false;

    /**
     * represent the path of keystore,
     * this path will be used to store the security keys
     * and certificates used for secure communication or cryptographic operations.
     */
    String keyStorePath();

    /**
     * represent the password of keystore,
     * this password will be used to unlock the keystore
     * for accessing cryptographic keys.
     */
    String keyStorePassword();

    /**
     * represent the path of truststore,
     * this path will be used to store the trusted certificates
     * used for secure communication or cryptographic operations.
     */
    String trustStorePath();

    /**
     * represent the password of truststore,
     * this password will be used to unlock the truststore
     * for accessing trusted certificates.
     */
    String trustStorePassword();

    /**
     * represent the type of authentication storage,
     * this type will be used to determine how user authentication information
     * (such as usernames, passwords, and roles) is stored and managed.
     */
    AuthStorageType type();

    /**
     * represent the URL of the database,
     * this URL will be used to connect to the database.
     */
    String databaseURL();

    /**
     * represent the username of the database,
     * this username will be used to connect to the database.
     */
    String databaseUsername();

    /**
     * represent the password of the database,
     * this password will be used to connect to the database.
     */
    String databasePassword();

    /**
     * represent the cooldown period for user chat interactions in seconds,
     * this value will be used to limit the frequency at which users can send messages
     * to prevent spam or overuse of the chat functionality.
     */
    String userChatCoolDown();
}
