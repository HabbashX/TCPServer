package com.habbashx.tcpserver.security;

import java.util.Set;

import static com.habbashx.tcpserver.logger.ConsoleColor.BRIGHT_RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.ORANGE;
import static com.habbashx.tcpserver.logger.ConsoleColor.PINK;
import static com.habbashx.tcpserver.security.utils.RolePermissionUtils.getAdministratorRolePermissions;
import static com.habbashx.tcpserver.security.utils.RolePermissionUtils.getDefaultRolePermissions;
import static com.habbashx.tcpserver.security.utils.RolePermissionUtils.getModeratorRolePermissions;
import static com.habbashx.tcpserver.security.utils.RolePermissionUtils.getOperatorRolePermissions;
import static com.habbashx.tcpserver.security.utils.RolePermissionUtils.getSuperAdministratorRolePermissions;

public enum Role {
    DEFAULT(getDefaultRolePermissions(), ""),
    MODERATOR(getModeratorRolePermissions(),PINK+"[Moderator] "),
    OPERATOR(getOperatorRolePermissions(),ORANGE+"[Operator] "),
    ADMINISTRATOR(getAdministratorRolePermissions(),BRIGHT_RED+"[Administrator] "),
    SUPER_ADMINISTRATOR(getSuperAdministratorRolePermissions(),BRIGHT_RED+"[Super-Administrator] ");

    private final Set<Integer> permissions;
    private final String prefix;

    Role(Set<Integer> permissions,String prefix) {
        this.prefix = prefix;
        this.permissions = permissions;
    }

    public Set<Integer> getPermissions() {
        return permissions;
    }

    public String getPrefix() {
        return prefix;
    }


}
