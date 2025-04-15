package com.habbashx.tcpserver.command;

import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.handler.console.ServerConsoleHandler;

public sealed abstract class CommandSender permits UserHandler, ServerConsoleHandler {

    public abstract boolean isConsole();
}
