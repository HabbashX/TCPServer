package com.habbashx.tcpserver.handler.connection.configuration;

import com.habbashx.annotation.InjectProperty;

/**
 * Represents a configuration class for managing connection-related settings.
 * <br>
 * This class encapsulates various connection properties, such as buffering, timeouts,
 * socket options, and more. It is designed to be used for configuring network connections
 * in a customizable and unified manner.
 * <br>
 * The properties in this class are dynamically injected from external sources using
 * the {@code @InjectProperty} annotation. This allows for the external configuration
 * of connection parameters via a configuration mechanism.
 * <br>
 * Designed to be immutable once constructed.
 */
public final class ConnectionSettings {

    /**
     * Indicates whether the socket timeout feature is enabled for network connections.
     * <br>
     * When enabled, the socket will enforce a timeout period for read and write operations,
     * defined by the associated timeout value. This can be used to prevent the application
     * from indefinitely waiting for a response when data transmission is delayed or interrupted.
     * <br>
     * This property is dynamically injected using the {@code @InjectProperty} annotation,
     * allowing its value to be configured externally, typically through a configuration file
     * or environment variable.
     */
    @InjectProperty("soTimeOutEnabled")
    private boolean soTimeOutEnabled;

    /**
     * Specifies the socket timeout duration in milliseconds.
     * This property is used to define the maximum period of inactivity
     * a connection can maintain before timing out. A value of zero
     * indicates that there is no timeout.
     * <br>
     * The value of this property is dynamically injected from an external
     * source via the {@code @InjectProperty} annotation.
     */
    @InjectProperty("soTimeout")
    private int soTimeout;

    /**
     * Indicates whether buffering is enabled for network connections.
     * This property, when set to {@code true}, enables data buffering which can
     * improve performance by batch processing data. If set to {@code false},
     * data is handled without buffering, potentially reducing latency but
     * possibly impacting throughput.
     *
     * The value is dynamically injected from an external configuration
     * source using the {@code @InjectProperty} annotation with the key
     * {@code "bufferingEnabled"}.
     */
    @InjectProperty("bufferingEnabled")
    private boolean bufferingEnabled;

    /**
     * Represents the size of the buffer to be used in the application.
     * This value is configurable and is injected via the @InjectProperty annotation.
     * It determines the amount of data the buffer can hold.
     */
    @InjectProperty("bufferSize")
    private int bufferSize;

    /**
     * Indicates whether the TCP_NODELAY option is enabled for the connection.
     * When enabled, this option disables Nagle's algorithm, minimizing latency
     * by sending packets immediately without buffering them. By default, Nagle's
     * algorithm is used to optimize TCP/IP performance by reducing the number of
     * packets sent, which may introduce a slight delay.
     *
     * This setting can be configured via an external property with the key "tcpNoDelay".
     */
    @InjectProperty("tcpNoDelay")
    private boolean tcpNoDelay;

    /**
     * Represents whether the connection should stay alive or not.
     *
     * The value of this variable determines whether the connection remains
     * persistent and prevents it from being closed after a single transaction.
     * This is often used to improve network performance by reusing existing
     * connections.
     *
     * The value is injected through the property "keepAlive".
     */
    @InjectProperty("keepAlive")
    private boolean keepAlive;

    /**
     * Determines whether the underlying socket will reuse the address
     * when binding. This is useful in scenarios where servers need to
     * be restarted and need to bind to the same port immediately without
     * waiting for the port to be released by the operating system.
     *
     * The value of this variable is injected using the "reuseAddress"
     * property from the configuration or dependency injection framework.
     */
    @InjectProperty("reuseAddress")
    private boolean reuseAddress;

    public boolean isSoTimeOutEnabled() {
        return soTimeOutEnabled;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public boolean isBufferingEnabled() {
        return bufferingEnabled;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }
}
