package com.habbashx.tcpserver.user;

import com.habbashx.tcpserver.security.Role;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class UserDetails {

    private String userIP;
    private String userID;
    private Role userRole;
    private String username;
    private String userEmail;
    private String phoneNumber;
    private boolean isActiveAccount;

    public UserDetails(String userIP, String userID, Role userRole, String username, String userEmail, String phoneNumber, boolean isActiveAccount) {
        this.userIP = userIP;
        this.userID = userID;
        this.userRole = userRole;
        this.username = username;
        this.userEmail = userEmail;
        this.phoneNumber = phoneNumber;
        this.isActiveAccount = isActiveAccount;
    }
    public UserDetails() {

    }

    public String getUserIP() {
        return userIP;
    }

    public void setUserIP(String userIP) {
        this.userIP = userIP;
    }
    public Role getUserRole() {
        return userRole;
    }
    public void setUserRole(Role userRole) {
        this.userRole = userRole;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean isActiveAccount() {
        return isActiveAccount;
    }

    public void setActiveAccount(boolean activeAccount) {
        isActiveAccount = activeAccount;
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull UserDetails.UserDetailsBuilder builder() {
        return new UserDetailsBuilder();
    }

    public static class UserDetailsBuilder {
        private String userIP;
        private String userID;
        private Role userRole;
        private String username;
        private String userEmail;
        private String phoneNumber;
        private boolean isActiveAccount;

        public UserDetailsBuilder userIP(String userIP) {
            this.userIP = userIP;
            return this;
        }

        public UserDetailsBuilder userRole(Role userRole) {
            this.userRole = userRole;
            return this;
        }

        public UserDetailsBuilder userID(String userID) {
            this.userID = userID;
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

        public UserDetailsBuilder activeAccount(boolean activeAccount) {
            isActiveAccount = activeAccount;
            return this;
        }

        public UserDetails build() {
            return  new UserDetails(userIP,userID,userRole,username,userEmail,phoneNumber,isActiveAccount);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof UserDetails that)) return false;
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
