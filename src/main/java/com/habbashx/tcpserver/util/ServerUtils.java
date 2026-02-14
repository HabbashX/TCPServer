package com.habbashx.tcpserver.util;


import com.habbashx.injector.PropertyInjector;

import java.io.File;

/**
 * Utility class for server-related constants and methods.
 * This class provides helper utilities for server configuration and settings.
 * <p>
 * The class is designed for static access and should not be instantiated.
 */
public final class ServerUtils {

    /**
     * Represents the file path to the properties file used
     * for configuring server settings.
     * This constant points to a file named "settings.properties"
     * located in the "settings" directory.
     */
    public static final String SERVER_SETTINGS_PATH = "server/settings/settings.properties";

    /**
     * Injects server settings from an external configuration file into the current User instance.
     * <p>
     * This method utilizes the `PropertyInjector` utility to read and inject properties
     * from a configuration file located at the path specified by `SERVER_SETTINGS_PATH`.
     * The injected properties configure the internal state of the application, enabling
     * it to adhere to the desired behavior and settings defined in the server configuration.
     * <p>
     * The method is designed to provide a streamlined way of centralizing configuration management
     * by ensuring that all properties specified in the configuration file are automatically
     * applied to the user's required fields or settings.
     * <p>
     * Throws a runtime exception in case of errors, such as:
     * - The configuration file is missing or cannot be read.
     * - There is a failure to inject the properties.
     * - Invalid configuration parameters are provided, which could hinder application functionality.
     * <p>
     * This method is invoked at the initialization phase of the `User` class to ensure all
     * necessary configurations are applied before further operation.
     *
     * @throws RuntimeException if an error occurs during the property injection process
     */
    public static void injectServerSettings(Object object) {
        PropertyInjector propertyInjector = new PropertyInjector(new File(ServerUtils.SERVER_SETTINGS_PATH));
        propertyInjector.inject(object);
    }

}
