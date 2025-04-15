package com.habbashx.tcpserver.security.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

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
    public static Set<Integer> getSuperAdministratorRolePermissions() {
        return getRolePermissions("super-administrator");
    }

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
