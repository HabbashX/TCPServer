package com.habbashx.tcpserver.user;

import com.habbashx.tcpserver.security.Role;

import java.util.Objects;

/**
 * Represents the details of a user including information such as IP address, ID, role,
 * username, email, phone number, and account status.
 * <p>
 * This class provides getter and setter methods for each field, along with a builder class
 * for constructing instances of UserDetails. It also overrides the equals and hashCode
 * methods for object comparison and hashing.
 * <p>
 * The class is declared as final to prevent inheritance and ensure immutability except for
 * the provided setter methods.
 */
public final class UserDetails {

    private final String userIP;
    private final String userID;
    private final Role userRole;
    private final String username;
    private final String userEmail;
    private final String phoneNumber;
    private final boolean isActiveAccount;

    private UserDetails(UserDetailsBuilder builder) {
        this.userIP = builder.userIP;
        this.userID = builder.userID;
        this.userRole = builder.userRole;
        this.username = builder.username;
        this.userEmail = builder.userEmail;
        this.phoneNumber = builder.phoneNumber;
        this.isActiveAccount = builder.isActiveAccount;
    }

    public String getUserIP() {
        return userIP;
    }

    public String getUserID() {
        return userID;
    }

    public Role getUserRole() {
        return userRole;
    }

    public String getUsername() {
        return username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isActiveAccount() {
        return isActiveAccount;
    }

    public static UserDetailsBuilder builder() {
        return new UserDetailsBuilder();
    }

    public static final class UserDetailsBuilder {
        private String userIP;
        private String userID;
        private Role userRole;
        private String username;
        private String userEmail;
        private String phoneNumber;
        private boolean isActiveAccount;

        private UserDetailsBuilder() {
        }

        public UserDetailsBuilder userIP(String userIP) {
            this.userIP = userIP;
            return this;
        }

        public UserDetailsBuilder userID(String userID) {
            this.userID = userID;
            return this;
        }

        public UserDetailsBuilder userRole(Role userRole) {
            this.userRole = userRole;
            return this;
        }

        public UserDetailsBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserDetailsBuilder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public UserDetailsBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public UserDetailsBuilder activeAccount(boolean isActiveAccount) {
            this.isActiveAccount = isActiveAccount;
            return this;
        }

        public UserDetails build() {
            return new UserDetails(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDetails that)) return false;
        return isActiveAccount == that.isActiveAccount &&
                Objects.equals(userIP, that.userIP) &&
                Objects.equals(userID, that.userID) &&
                userRole == that.userRole &&
                Objects.equals(username, that.username) &&
                Objects.equals(userEmail, that.userEmail) &&
                Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userIP, userID, userRole, username, userEmail, phoneNumber, isActiveAccount);
    }
}
