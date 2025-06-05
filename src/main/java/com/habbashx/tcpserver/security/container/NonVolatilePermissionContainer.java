package com.habbashx.tcpserver.security.container;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habbashx.tcpserver.handler.UserHandler;
import com.habbashx.tcpserver.security.container.annotation.Container;
import com.habbashx.tcpserver.security.container.manager.ContainerManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.habbashx.tcpserver.socket.Server.getInstance;

/**
 * This class manages non-volatile permission data for users, leveraging a JSON-based
 * storage file specified by the {@link Container} annotation. It provides functionality
 * to add, remove, and retrieve user permissions, ensuring consistent updates to the
 * storage file and maintaining runtime synchronization.
 * <p>
 * The permissions are associated with a specific user identified by the UserHandler instance.
 * The class relies on the Jackson library for parsing and persisting the permissions data
 * in a formatted JSON file.
 * <p>
 * The file path for storing the permissions is retrieved from the {@link Container} annotation
 * applied to the class.
 */
@SuppressWarnings("unchecked")
@Container(file = "containers/permissions/usersPermissions.json")
public class NonVolatilePermissionContainer extends ContainerManager {

    private final UserHandler userHandler;

    /**
     * A list of integer values representing permissions for the associated user.
     * This list is initialized and managed through the `NonVolatilePermissionContainer` class,
     * loading data from a non-volatile file defined by the {@code @Container} annotation.
     * <p>
     * The permissions list may be modified through methods such as {@code addPermission}
     * and {@code removePermission}.
     * <p>
     * Note that updates to this list persist changes to the non-volatile file.
     */
    private List<Integer> permissions = new ArrayList<>();

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Represents a non-volatile file used to persistently store user permissions.
     * The file path is retrieved from the {@link Container} annotation on the containing class.
     * This file is utilized to read, modify, and save user permissions in a structured format,
     * ensuring that data changes are preserved across application restarts.
     */
    private final File nonVolatilePermissionFile = getContainerFile();

    /**
     * Constructor for the NonVolatilePermissionContainer class.
     * Initializes the container with user-specific permission handling
     * and loads the associated persistent permissions file.
     *
     * @param userHandler an instance of UserHandler responsible for managing user-related operations.
     */
    public NonVolatilePermissionContainer(UserHandler userHandler) {
        this.userHandler = userHandler;
        initPermissions();
    }

    /**
     * Adds a permission to the current permission list and updates the persistent storage
     * to reflect the change.
     *
     * @param permission The permission value to be added.
     * @return true if the permission was successfully added and persisted, false otherwise.
     */
    public boolean addPermission(int permission) {
        permissions.add(permission);
        return rewritePermission(permission, true);
    }

    /**
     * Removes a specified permission from the current user.
     *
     * @param permission The permission ID to be removed.
     * @return {@code true} if the permission was successfully removed and updated,
     * {@code false} otherwise.
     */
    public boolean removePermission(int permission) {
        permissions.remove(permission);
        return rewritePermission(permission, false);
    }

    /**
     * Checks if the specified permission exists in the current list of permissions.
     *
     * @param permission the ID of the permission to be checked.
     * @return true if the permission is found in the permissions list, false otherwise.
     */
    public boolean hasPermission(int permission) {
        return permissions.contains(permission);
    }

    /**
     * Retrieves the list of permissions associated with the container.
     *
     * @return a list of permission integers, or null if no permissions are available.
     */
    public @Nullable List<Integer> getPermissions() {
        return permissions;
    }

    /**
     * Updates the permission list for the current user by either adding or removing a specific permission,
     * and writes the updated permission list to the non-volatile storage file.
     *
     * @param permission the permission to add or remove from the user's permission list
     * @param add        a boolean flag indicating the operation (true to add the permission, false to remove it)
     * @return true if the permission was successfully updated and stored, false if an error occurs or the user ID is not found
     */
    private boolean rewritePermission(int permission, boolean add) {
        try {
            List<Map<String, Object>> per = mapper.readValue(
                    nonVolatilePermissionFile,
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );

            boolean userUpdated = false;


            for (Map<String, Object> map : per) {
                if (map.get("userID").equals(userHandler.getUserDetails().getUserID())) {

                    List<Integer> userPermissions = (List<Integer>) map.get("permissions");

                    if (add) {
                        if (!userPermissions.contains(permission)) {
                            userPermissions.add(permission);
                        } else {
                            return false;
                        }
                    } else {
                        if (userPermissions.contains(permission)) {
                            userPermissions.remove((Integer) permission);
                        } else {
                            return false;
                        }
                    }


                    map.replace("permissions", userPermissions);
                    userUpdated = true;
                    break;
                }
            }

            if (!userUpdated) {
                Map<String, Object> newMap = new HashMap<>();
                newMap.put("userID", userHandler.getUserDetails().getUserID());
                newMap.put("permissions", Collections.singletonList(permission));
                per.add(newMap);
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(nonVolatilePermissionFile, per);

            initPermissions();

            return true;
        } catch (IOException e) {
            getInstance().getServerLogger().error(e);
            return false;
        }
    }

    /**
     * Initializes the user permissions from a non-volatile permission file.
     * This method reads the permissions data from a file, filters the data
     * to locate permissions for a specific user ID, and assigns the permissions
     * to the corresponding instance field.
     * <p>
     * The method handles possible I/O exceptions and logs any errors using
     * the server's logger.
     * <p>
     * Workflow:
     * 1. Reads the permissions file and deserializes it into a list of maps.
     * 2. Filters the list to find the permissions associated with the targeted user ID.
     * 3. Assigns the retrieved permissions list to the instance's permissions field.
     * 4. In case of an error during file reading or JSON parsing, logs the error.
     */
    private void initPermissions() {
        try {

            List<Map<String, Object>> per = mapper.readValue(nonVolatilePermissionFile, new TypeReference<List<Map<String, Object>>>() {
            });

            permissions = per.stream()
                    .filter(map -> map.get("userID").equals(userHandler.getUserDetails().getUserID()))
                    .map(map -> (List<Integer>) map.get("permissions"))
                    .findFirst().orElse(null);

            if (permissions == null) {
                permissions = new ArrayList<>();
            }

        } catch (IOException e) {
            getInstance().getServerLogger().error(e);
        }
    }
}
