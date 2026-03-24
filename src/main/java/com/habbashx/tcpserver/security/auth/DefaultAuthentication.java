package com.habbashx.tcpserver.security.auth;

import com.habbashx.tcpserver.connection.UserHandler;
import com.habbashx.tcpserver.event.AuthenticationEvent;
import com.habbashx.tcpserver.security.Role;
import com.habbashx.tcpserver.security.auth.storage.UserStorage;
import com.habbashx.tcpserver.security.auth.storage.UserStorageFactory;
import com.habbashx.tcpserver.socket.server.Server;
import com.habbashx.tcpserver.user.UserDetails;
import org.jetbrains.annotations.NotNull;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Clean, pattern-based DefaultAuthentication implementation.
 */
public final class DefaultAuthentication extends Authentication {

    private final Server server;
    private final UserStorage storage;
    private final List<UserValidator> validators = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public DefaultAuthentication(@NotNull Server server) {
        this.server = server;

        assert server.getServerSettings().getAuthStorageType() != null;
        this.storage = UserStorageFactory.create(server.getServerSettings().getAuthStorageType(), server);

        validators.add(new UsernameValidator());
        validators.add(new EmailValidator());
        validators.add(new PhoneValidator());
    }

    @Override
    public void register(@NotNull String username, @NotNull String password, String email, String phoneNumber, @NotNull UserHandler userHandler) {
        lock.lock();
        try {
            UserDetails details = UserDetails.builder()
                    .username(username)
                    .userEmail(email)
                    .phoneNumber(phoneNumber)
                    .userRole(Role.DEFAULT)
                    .activeAccount(true)
                    .build();

            for (UserValidator validator : validators) {
                validator.validate(details);
            }
            if (storage.isUserExists(username)) {
                userHandler.sendTextMessage("User already exists!");
                return;
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            storage.registerUser(details, hashedPassword);

            userHandler.setUserDetails(details);

            server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler, true, true));

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void login(@NotNull String username, @NotNull String password, @NotNull UserHandler userHandler) {
        lock.lock();
        try {
            if (server.getAuthenticatedUsers().containsKey(username)) {
                userHandler.sendTextMessage("User already connected!");
                userHandler.shutdown();
                return;
            }

            UserDetails details = storage.getUser(username);
            if (details == null) {
                server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler, false, false));
                return;
            }

            String hashed = storage.getHashedPassword(username);
            if (BCrypt.checkpw(password, hashed)) {
                userHandler.setUserDetails(details);
                server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler, true, false));
            } else {
                server.getEventManager().triggerEvent(new AuthenticationEvent(userHandler, false, false));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    interface UserValidator {
        void validate(UserDetails details);
    }

    static final class UsernameValidator implements UserValidator {
        @Override
        public void validate(UserDetails details) {
            if (details.getUsername() == null || !details.getUsername().matches("^[a-zA-Z0-9_]{3,20}$")) {
                throw new IllegalArgumentException("Invalid username");
            }
        }
    }

    static final class EmailValidator implements UserValidator {
        @Override
        public void validate(UserDetails details) {
            if (details.getUserEmail() != null && !details.getUserEmail().matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$")) {
                throw new IllegalArgumentException("Invalid email");
            }
        }
    }

    static final class PhoneValidator implements UserValidator {
        @Override
        public void validate(UserDetails details) {
            if (details.getPhoneNumber() != null && !details.getPhoneNumber().matches("^[0-9+]{7,15}$")) {
                throw new IllegalArgumentException("Invalid phone number");
            }
        }
    }
}