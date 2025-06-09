package com.habbashx.tcpserver.user;

import com.habbashx.tcpserver.security.Role;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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

    /**
     * Represents the IP address of the user.
     * <p>
     * This variable stores the IP address from which the user is accessing the system.
     * It is used for purposes such as tracking user sessions, logging, and security checks.
     */
    private String userIP;
    /**
     * Represents the unique identifier for a user within the system.
     * <p>
     * The userID is a string that uniquely identifies each user and is used
     * for operations such as authentication, authorization, and tracking user-specific
     * activities within the application.
     */
    private String userID;
    /**
     * Represents the role assigned to a user within the system.
     * <p>
     * The user role determines the user's access privileges and defines their
     * responsibilities, as well as the operations they are permitted to perform.
     * The value of this variable is defined by the {@link Role} enumeration,
     * which includes predefined roles such as DEFAULT, MODERATOR, OPERATOR,
     * ADMINISTRATOR, and SUPER_ADMINISTRATOR.
     * <p>
     * This field is typically set during the creation of a user account and
     * can be updated as needed to reflect the user's role in the system.
     */
    private Role userRole;
    /**
     * Represents the username of a user.
     * This variable stores the unique identifier or name used to authenticate
     * or identify a user within the system.
     */
    private String username;
    /**
     * Represents the email address associated with the user.
     * <p>
     * This variable stores the user's email, which can be utilized for
     * user identification, communication, and account-related notifications.
     * It is expected to be a valid email address format and uniquely identify
     * the user within the system.
     */
    private String userEmail;
    /**
     * Stores the phone number associated with the user.
     * <p>
     * This variable represents the user's phone number as a string, which can be used
     * for purposes such as contact information or account verification. The format
     * and validation of the phone number may depend on the system's requirements.
     */
    private String phoneNumber;
    /**
     * Indicates whether the user account is currently active.
     * <p>
     * This variable is used to determine the operational status of a user account within the system.
     * An active account implies that the user is authorized to access and utilize system resources,
     * while an inactive account may be disabled or restricted from accessing certain functionalities.
     */
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
            return new UserDetails(userIP, userID, userRole, username, userEmail, phoneNumber, isActiveAccount);
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
