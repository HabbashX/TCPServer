package com.habbashx.tcpserver.listener;

import com.habbashx.tcpserver.event.Event;

public interface Listener<E extends Event> {
    void onEvent(E event);
}
