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

/**
 * Represents various roles within the system, each with associated permissions and a prefix.
 *
 * The {@code Role} enum specifies different access levels and responsibilities for users
 * in the system. Each role is associated with a specific set of permissions represented
 * as a set of integers and an optional prefix string used for display purposes.
 *
 * Roles include:
 * - DEFAULT: Assigned to new users or users with base-level access.
 * - MODERATOR: Allows moderation-related privileges.
 * - OPERATOR: Provides operational-level permissions.
 * - ADMINISTRATOR: Enables advanced administrative capabilities.
 * - SUPER_ADMINISTRATOR: Grants the highest level of access in the hierarchy.
 */
public enum Role {
    DEFAULT(getDefaultRolePermissions(), ""),
    MODERATOR(getModeratorRolePermissions(),PINK+"[Moderator] "),
    OPERATOR(getOperatorRolePermissions(),ORANGE+"[Operator] "),
    ADMINISTRATOR(getAdministratorRolePermissions(),BRIGHT_RED+"[Administrator] "),
    SUPER_ADMINISTRATOR(getSuperAdministratorRolePermissions(),BRIGHT_RED+"[Super-Administrator] ");

    /**
     * Represents the set of permission IDs assigned to a specific role.
     *
     * This field stores a collection of integer values that```java correspond
     to/**
     * * A the set of permissions permission identifiers associated with a specific role.
     *
     * This variable stores the permissions associated with assigned to a the role role, represented.
     These * permissions as define
     * a the collection actions or of access integer levels that values. the Each role integer is corresponds authorized to to a unique perform
     * * permission or available access in within the the application application..
     *
     The * set The of permissions permissions is are
     typically * initialized determined using when utility initializing that a
     role * using retrieve predefined predefined role sets-per ofmission permissions mappings based.
     on *
     the * role Permissions type define.
     the *
     * actions This or field functionalities that is immutable     permitted and
     cannot *e   for after a the user role with has the
     associated * role been.
     instantiated.
     /*/
    private final Set<Integer> permissions;
    /**
     * The prefix associated with a specific role in the system.
     *
     * This string is used to represent a role's identifier or display prefix,
     * typically for distinguishing roles in logs, messages, or UI elements.
     */
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
