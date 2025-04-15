package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.AuthenticationEvent;
import com.habbashx.tcpserver.event.UserJoinEvent;
import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.listener.Listener;

import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.LIME_GREEN;
import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

@EventHandler
public final class AuthenticationEventHandler implements Listener<AuthenticationEvent> {

    @Override
    public void onEvent(@NotNull AuthenticationEvent event) {

        final String username = event.getUserHandler().getUserDetails().getUsername();
        final UserJoinEvent userJoinEvent = new UserJoinEvent(username,event.getUserHandler());
        if (event.isRegisterOperation()) {
            if (event.isAuthenticated()) {
                event.getUserHandler().sendMessage(LIME_GREEN + "Register Successfully" + RESET);
                event.getUserHandler().getServer().getEventManager().triggerEvent(userJoinEvent);
            } else {
                event.getUserHandler().sendMessage(RED+"this username already registered in system");
                event.getUserHandler().shutdown();
            }
        } else if (event.isAuthenticated())  {
            event.getUserHandler().sendMessage(LIME_GREEN+"login successfully"+RESET);
            event.getUserHandler().getServer().getEventManager().triggerEvent(userJoinEvent);
        } else {
            event.getUserHandler().sendMessage(RED+"wrong username or password"+RESET);
            event.getUserHandler().shutdown();
        }
    }

}
