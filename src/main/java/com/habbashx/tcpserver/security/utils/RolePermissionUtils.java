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
public class RolePermissionUtils {

    public static Set<Integer> getDefaultRolePermissions() {
        return getRolePermissions("default");
    }
    public static Set<Integer> getModeratorRolePermissions() {
        return getRolePermissions("moderator");
    }
    public static Set<Integer> getOperatorRolePermissions() {
        return getRolePermissions("operator");
    }
    public static Set<Integer> getAdministratorRolePermissions() {
        return getRolePermissions("administrator");
    }


    /**
     * Retrieves the permissions associated with the "super-administrator" role.
     *
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
     *
     * The method reads from a JSON file containing role-to-permissions mappings
     * and returns the set of permission IDs assigned to the specified role.
     *
     * @param role the name of the role for which permissions should be retrieved
     * @return a set of permission IDs*/
    private static @Nullable Set<Integer> getRolePermissions(String role) {

        final File file = new File("data/role-permissions.json");
        final ObjectMapper mapper = new ObjectMapper();
        try {
            Set<Map<String, Set<Integer>>> roles = mapper.readValue(file, new TypeReference<>() {
            });

            for (Map<String, Set<Integer>> r : roles) {
                if (r.containsKey(role)) {
                    return r.get(role);
                }
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
