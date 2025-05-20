package com.habbashx.tcpserver.security;


/**
 * The Permission class defines various constants representing
 * different types of permissions within a system. Each constant
 * represents a specific action or privilege that can be granted
 * or restricted to users or roles.
 */
public class Permission {

    public static final int NO_PERMISSION_REQUIRED = 0X00;
    public static final int BAN_PERMISSION = 0X01;
    public static final int UN_BAN_PERMISSION = 0x02;
    public static final int MUTE_PERMISSION = 0x03;
    public static final int UN_MUTE_PERMISSION = 0X04;
    public static final int CHANGE_RANK_PERMISSION = 0X05;
    public static final int NICKNAME_PERMISSION = 0X06;
    public static final int NO_PERMISSION =  0X0EFA;
}

