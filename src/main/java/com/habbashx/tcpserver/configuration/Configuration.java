package com.habbashx.tcpserver.configuration;

/**
 * An abstract class that represents a configuration management system. This class provides
 * a blueprint for defining how configurations are accessed and modified.
 */
public abstract class Configuration {

    public abstract Object returnValue(String element);
    public abstract void modify(String element ,String newValue);
}
