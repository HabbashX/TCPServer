package com.habbashx.tcpserver.command.configuration;

public abstract class Configuration {

    public abstract Object returnValue(String element);
    public abstract void modify(String element ,String newValue);
}
