package com.habbashx.tcpserver.security;

import com.habbashx.tcpserver.handler.UserHandler;
import org.jetbrains.annotations.NotNull;

public sealed abstract class Authentication permits DefaultAuthentication {
    public abstract void register(@NotNull String username, @NotNull String password, String email, String phoneNumber ,@NotNull UserHandler userHandler);
    public abstract void login(@NotNull String username , @NotNull String password ,@NotNull UserHandler userHandler);
}
