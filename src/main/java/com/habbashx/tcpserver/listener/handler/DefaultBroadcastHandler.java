package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.Priority;
import com.habbashx.tcpserver.delayevent.BroadcastEvent;
import com.habbashx.tcpserver.delayevent.handler.DelayEventHandler;
import com.habbashx.tcpserver.listener.DelayListener;
import org.jetbrains.annotations.NotNull;

@DelayEventHandler(priority = Priority.LOW , delayMilliSeconds = 6000L)
public final class DefaultBroadcastHandler implements DelayListener<BroadcastEvent> {

    @Override
    public void onEvent(@NotNull BroadcastEvent event) {
        event.getServer().broadcast(event.getBroadcastMessage());
    }

}
