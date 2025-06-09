package com.habbashx.tcpserver.security.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for managing role-based permissions.
 * Provides methods to retrieve predefined sets of permissions
 * associated with specific user roles within the application.
 */
public final class RolePermissionUtils {

    /**
     * Retrieves the permissions associated with the "default" role.
     *
     * This method provides the set of permission IDs assigned specifically
     * to the "default" role in the application.
     *
     * @return a set of integer permission IDs for the "default" role
     */
    public static Set<Integer> getDefaultRolePermissions() {
        return getRolePermissions("default");
    }

    /**
     * Retrieves the permissions associated with the "moderator" role.
     *
     * @return a set of integer permission IDs for the "moderator" role
     */
    public static Set<Integer> getModeratorRolePermissions() {
        return getRolePermissions("moderator");
    }

    /**
     * Retrieves the permissions associated with the "operator" role.
     *
     * This method provides a set of permission IDs assigned to users
     * with the "operator" role in the application. It uses the underlying
     * role-to-permissions mapping mechanism to fetch the associated permissions.
     *
     * @return a set of integer permission IDs for the "operator" role
     */
    public static Set<Integer> getOperatorRolePermissions() {
        return getRolePermissions("operator");
    }

    /**
     * Retrieves the permissions associated with the "administrator" role.
     *
     * This method provides the set of permission IDs assigned specifically
     * to the "administrator" role in the application by utilizing
     * the internal role-to-permissions mapping.
     *
     * @return a set of integer permission IDs for the "administrator" role
     */
    public static Set<Integer> getAdministratorRolePermissions() {
        return getRolePermissions("administrator");
    }


    /**
     * Retrieves the permissions associated with the "super-administrator" role.
     * <p>
     * This method provides the set of permission IDs assigned specifically
     * to the "super-administrator" role in the application.
     *
     * @return a set of integer permission IDs for the "super-administrator" role
     */
    public static Set<Integer> getSuperAdministratorRolePermissions() {
        return getRolePermissions("super-administrator");
    }

    /**
     * Retrieves the permissions associated with a given role.
     * <p>
     * The method reads from a JSON file containing role-to-permissions mappings
     * and returns the set of permission IDs assigned to the specified role.
     *
     * @param role the name of the role for which permissions should be retrieved
     * @return a set of permission IDs
     */
    private static @Nullable Set<Integer> getRolePermissions(String role) {

        final File file = new File("data/role-permissions.json");
        final ObjectMapper mapper = new ObjectMapper();
        try {
            Set<Map<String, Set<Integer>>> roles = mapper.readValue(file, new TypeReference<>() {
            });

            return roles.stream()
                    .filter(r -> r.containsKey(role))
                    .findFirst()
                    .map(r -> r.get(role))
                    .orElse(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
