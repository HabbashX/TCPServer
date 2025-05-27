package com.habbashx.tcpserver.handler.connection;

/**
 * Represents a handler for managing a connection between a client and a server.
 * This interface defines the core functionality required to process and manage
 * the lifecycle of a connection, including communication, user interaction,
 * and resource management.
 *
 * Implementations of this interface are responsible for:
 * - Processing client input and output streams.
 * - Managing user authentication and session activity.
 * - Executing commands or handling events during the connection.
 * - Providing mechanisms for sending messages to the client.
 * - Safely shutting down connections and cleaning up resources when required.
 *
 * Core Features:
 * - Acts as a bridge between a client and the associated server.
 * - Supports interaction through commands and chat message handling.
 * - Includes thread-safe mechanisms for concurrent operations via locks.
 *
 * Typical implementations of this handler should ensure correct handling of:
 * - Input/output stream initialization and cleanup.
 * - Authentication checks and session management.
 * - Error handling and resource deallocation in abnormal circumstances.
 */
public interface ConnectionHandler {

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
