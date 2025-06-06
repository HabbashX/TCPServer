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
    /**
     * Represents the default role assigned to users with base-level access.
     *
     * The DEFAULT role is typically assigned to new users or users
     * with minimal permissions. It initializes with a predefined
     * set of permissions obtained from the {@code getDefaultRolePermissions()}
     * method and an empty string as its display prefix.
     */
    DEFAULT(getDefaultRolePermissions(), ""),
    /**
     * Represents the "Moderator" role within the system.
     *
     * This role is assigned to users who have privileges to perform moderation-related
     * actions, such as managing content or overseeing user interactions. The permissions
     * for this role are retrieved through the {@code getModeratorRolePermissions()} method,
     * which provides the specific set of permissions granted to this role.
     *
     * Additionally, this role is visually denoted with a prefix, displayed as "[Moderator]"
     * in pink color, aiding in identifying users with this role in the user interface or logs.
     */
    MODERATOR(getModeratorRolePermissions(),PINK+"[Moderator] "),
    /**
     * Represents the operator role within the system.
     *
     * This role provides permissions and operational privileges above the default user level
     * but below administrative tiers. It includes access to functionalities and tasks
     * necessary for operational management within the application.
     *
     * The associated set of permissions is retrieved using {@code getOperatorRolePermissions()}
     * from {@code RolePermissionUtils}, which loads predefined permission mappings
     * for the "operator" role from external configuration, such as a JSON file.
     *
     * The prefix "[Operator]" is assigned to distinguish this role in logs, messages,
     * or user interfaces.
     */
    OPERATOR(getOperatorRolePermissions(),ORANGE+"[Operator] "),
    /**
     * Represents the "Administrator" role in the system.
     *
     * The "Administrator" role is characterized by advanced administrative capabilities
     * and privileges. It provides the role-specific display prefix and permissions set
     * to manage and oversee various operations in the application.
     *
     * Permissions for this role are retrieved using the {@code getAdministratorRolePermissions()}
     * method, which defines the specific actions or functionalities that an Administrator
     * is authorized to perform.
     *
     * The display prefix for the Administrator role is defined as a bright red
     * "[Administrator]" string, typically used for distinguishing this role
     * in user interfaces, log messages, or other contexts.
     */
    ADMINISTRATOR(getAdministratorRolePermissions(),BRIGHT_RED+"[Administrator] "),
    /**
     * A constant representing the role of a Super Administrator in the system.
     * This role typically has elevated privileges and permissions, as defined
     * by the associated method `getSuperAdministratorRolePermissions()`.
     * The role is displayed with a visual identifier combining a color code
     * and a label "[Super-Administrator]".
     */
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

    /**
     * Constructs a Role object with the specified permissions and prefix.
     *
     * @param permissions the set of permissions associated with the role
     * @param prefix the prefix associated with the role
     */
    private Role(Set<Integer> permissions,String prefix) {
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
