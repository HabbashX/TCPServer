package com.habbashx.tcpserver.listener;

import com.habbashx.tcpserver.delayevent.DelayEvent;

public interface DelayListener<E extends DelayEvent> {
    void onEvent(E event);
}
