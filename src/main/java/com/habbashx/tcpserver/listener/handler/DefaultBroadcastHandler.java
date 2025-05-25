package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.Priority;
import com.habbashx.tcpserver.delayevent.BroadcastEvent;
import com.habbashx.tcpserver.delayevent.handler.DelayEventHandler;
import com.habbashx.tcpserver.listener.DelayListener;
import org.jetbrains.annotations.NotNull;

/**
 * DefaultBroadcastHandler is a final implementation of the {@link DelayListener} interface,
 * designed to handle {@link BroadcastEvent} instances with a specified delay and low priority.
 *
 * This class is annotated with {@link DelayEventHandler}, which configures its processing
 * to occur with a delay of 6000 milliseconds when the event is triggered. The priority for
 * this handler is set to {@code Priority.LOW}.
 *
 * As a listener, it listens for {@link BroadcastEvent}, retrieves the broadcast message
 * and target server from the event instance, and triggers the broadcast operation to send
 * the message to connected users.
 *
 * This class is immutable and thread-safe since it contains no modifiable state.
 *
 * Responsibilities:
 * - Implements the primary logic of broadcasting messages by overriding the onEvent method.
 * - Coordinates with the {@link BroadcastEvent}, extracting necessary details to perform the broadcasting action.
 * - Works in conjunction with the delay event management system for scheduled processing of events.
 */
@DelayEventHandler(priority = Priority.LOW , delayMilliSeconds = 6000L)
public final class DefaultBroadcastHandler implements DelayListener<BroadcastEvent> {

    @Override
    public void onEvent(@NotNull BroadcastEvent event) {
        event.getServer().broadcast(event.getBroadcastMessage());
    }

}
