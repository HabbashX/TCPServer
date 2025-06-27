package com.habbashx.tcpserver;

/**
 * The {@code Shutdownable} interface defines a contract for classes that can be shut down.
 * Implementing classes must provide a method to perform shutdown operations, which may include
 * releasing resources, closing connections, or performing cleanup tasks.
 */
public interface Shutdownable {

    /**
     * Performs the shutdown operation for the implementing class.
     * This method should handle any necessary cleanup, resource release,
     * or other shutdown tasks.
     *
     * @throws Exception if an error occurs during the shutdown process.
     */
    void shutdown() throws Exception;
}
