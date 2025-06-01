package com.habbashx.tcpserver.handler.connection;

import com.habbashx.tcpserver.security.Permissible;

/**
 * Represents a handler responsible for managing a specific type of connection.
 * Implementing classes are intended to provide functionality for connection
 * lifecycle management, user interaction, and communication handling.
 * The `ConnectionHandler` interface extends the `Permissible` interface,
 * enabling permission-based checks for operations.
 */
public interface ConnectionHandler extends Permissible {

    /**
     * Retrieves the type of the handler represented by the implementing class.
     * The handler type usually indicates the specific role or functionality
     * the handler provides within the connection management process.
     *
     * @return a string representation of the handler type.
     */
    String getHandlerType();

    /**
     * Retrieves a description of the handler. The description typically provides
     * meaningful information about the purpose or functionality of the handler.
     *
     * @return a string containing the description of the handler.
     */
    String getHandlerDescription();

}
