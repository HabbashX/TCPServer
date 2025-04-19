module TCPServer {
    requires com.fasterxml.jackson.databind;
    requires jbcrypt;
    requires org.apache.commons.csv;
    requires org.jetbrains.annotations;
    requires org.yaml.snakeyaml;
    requires java.sql;
    requires java.desktop;
    requires java.management.rmi;
    requires mysql.connector.j;
    requires property.parser;

    exports com.habbashx.tcpserver.security;
    exports com.habbashx.tcpserver.socket;
    exports com.habbashx.tcpserver.command;
    exports com.habbashx.tcpserver.command.defaultcommand;
    exports com.habbashx.tcpserver.command.manager;
    exports com.habbashx.tcpserver.event;
    exports com.habbashx.tcpserver.event.handler;
    exports com.habbashx.tcpserver.event.manager;
    exports com.habbashx.tcpserver.user;
    exports com.habbashx.tcpserver.settings;
    exports com.habbashx.tcpserver.listener;
    exports com.habbashx.tcpserver.listener.handler;
    exports com.habbashx.tcpserver.util;
    exports com.habbashx.tcpserver.delayevent;
    exports com.habbashx.tcpserver.delayevent.manager;
    exports com.habbashx.tcpserver.delayevent.handler;
    exports com.habbashx.tcpserver.logger;
    exports com.habbashx.tcpserver.handler;
    exports com.habbashx.tcpserver.handler.console;
    exports com.habbashx.tcpserver.cooldown;
    exports com.habbashx.tcpserver.command.configuration;
    exports com.habbashx.tcpserver.annotation;

    opens com.habbashx.tcpserver.socket;
    opens com.habbashx.tcpserver.settings;
}