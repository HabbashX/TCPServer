package com.habbashx.tcpserver.security;

/**
 * Represents an entity that can be evaluated for permissions.
 */
public interface Permissible {

    /**
     * Checks if the entity has the specified permission.
     *
     * @param permission the permission to be checked, represented as an integer
     * @return true if the entity has the specified permission, false otherwise
     */
    boolean hasPermission(int permission);
}
