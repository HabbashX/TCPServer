package com.habbashx.tcpserver.handler.connection.util;

import com.habbashx.tcpserver.handler.connection.ConnectionHandler;
import com.habbashx.tcpserver.logger.ServerLogger;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class containing helper methods for managing connection handler files
 * and operations related to connection configurations.
 */
public class ConnectionUtils {

    /**
     * Generates a connection handler settings file if it does not exist.
     * This method checks for the existence of a file specific to the provided
     * {@code ConnectionHandler}. If the file does not exist, it creates the file, writes
     * default configuration settings to it, and logs the creation action using the {@code ServerLogger}.
     *
     * @param connectionHandler the connection handler for which the settings file is being managed.
     *                          It contains specific connection details used to generate the file.
     * @param serverLogger      the logger instance used to log information about file creation
     *                          and any errors that may occur during the file operations.
     */
    public static void generateConnectionHandlerFile(@NotNull ConnectionHandler connectionHandler, ServerLogger serverLogger) {
        final String fileName = getConnectionHandlerFile(connectionHandler);

        final Path path = Path.of(fileName);

        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
                var writer = getWriter(path);
                writer.close();
                serverLogger.info(connectionHandler + "settings file have been created");
            } catch (IOException e) {
                serverLogger.error(e);
            }
        }
    }

    /**
     * Creates and returns a {@link FileWriter} instance for the specified file path.
     * This method writes a default configuration template to the file before returning the writer.
     *
     * @param path the {@link Path} representing the file to which the writer should write.
     *             Must not be null.
     * @return a {@link FileWriter} instance ready for writing to the specified file.
     *         Will never return null.
     * @throws IOException if an I/O error occurs while creating the writer or writing to the file.
     */
    private static @NotNull FileWriter getWriter(@NotNull Path path) throws IOException {
        FileWriter writer = new FileWriter(path.toFile());
        writer.write("""
                connection.soTimeOutEnabled = false
                connection.soTimeout = 10000
                
                connection.bufferingEnabled = false
                connection.bufferSize = 1024
                
                connection.tcpNoDelay = false
                connection.keepAlive = false
                connection.reuseAddress = false
                """);
        return writer;
    }

    /**
     * Generates the file path for the connection handler's configuration properties file.
     * The file path is constructed based on the handler's class name, appended with
     * ".properties", and placed under the "connection/settings/" directory.
     *
     * @param connectionHandler the connection handler for which the configuration file path
     *                          is being generated. This parameter must not be null.
     * @return the file path as a string, pointing to the configuration properties file for
     * the specified connection handler. The return value is never null.
     */
    public static @NotNull String getConnectionHandlerFile(@NotNull ConnectionHandler connectionHandler) {

        return "connection/settings/" + connectionHandler.getClass().getSimpleName() + ".properties";
    }
}
