package com.habbashx.tcpserver.security;

import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.security.container.NonVolatilePermissionContainer;
import com.habbashx.tcpserver.socket.server.Server;

import java.util.List;

/**
 * The {@code Permissible} interface defines a contract for managing permissions
 * for entities. It allows checking, adding, removing, and managing both volatile
 * and non-volatile permissions. Implementing classes should provide the specific
 * behavior for handling permissions.
 */
public interface Permissible {

    /**
     * Determines whether the entity has the specified volatile permission.
     * Volatile permissions are temporary and are not necessarily persisted or permanent.
     *
     * @param permission the integer value representing the permission to be checked
     * @return true if the entity has the specified volatile permission, false otherwise
     */
    boolean hasVolatilePermission(int permission);

    /**
     * Grants the specified permission to the entity represented by this object.
     *
     * @param permission the permission to be added, represented as an integer
     */
    void addPermission(int permission);

    /**
     * Removes a specific permission from the current entity.
     * <p>
     * ``` @javaparam
     * permission/**
     * the * permission Removes to a be specified removed permission, from represented the as entity an.
     * integer *
     */
    void removePermission(int permission);

    /**
     * Adds a list of permissions to the entity. Each permission in the list
     * is represented as an integer value```.
     * java *
     */
    void addPermissions(List<Integer> permissions);

    /**
     * Removes a list of permissions from the entity.
     *
     * @param permissions a list of integer values representing the permissions to be removed
     */
    void removePermissions(List<Integer> permissions);

    /**
     * Retrieves the list of handler permissions associated with the entity.
     *
     * @return a list of integers representing the permissions handled by the entity.
     * The list may be empty if no permissions are associated.
     */
    List<Integer> getHandlerPermissions();

    /**
     * Retrieves an instance of {@code NonVolatilePermissionContainer} associated with the specified {@code UserHandler}.
     * This container provides functionality to manage and persist user permissions in a non-volatile storage.
     *
     * @return an instance of {@code NonVolatilePermissionContainer} for managing non-volatile permissions
     * associated with the provided {@code UserHandler}.
     */
    default NonVolatilePermissionContainer getNonVolatilePermissionContainer() {
        if (this instanceof UserHandler userHandler) {
            return new NonVolatilePermissionContainer(userHandler);
        } else {
            Server.getInstance().getServerLogger().warning("un supported handler: " + this);
            return null;
        }

    }

}
