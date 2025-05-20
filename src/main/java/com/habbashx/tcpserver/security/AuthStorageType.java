package com.habbashx.tcpserver.security;

/**
 * Represents the different types of storage mechanisms that can be used
 * for authentication data.
 *
 * This enum is utilized to determine how user authentication information
 * (such as usernames, passwords, and roles) is stored and managed.
 * The available storage types are as follows:
 *
 * 1. CSV - Data is stored in a CSV file.
 * 2. SQL - Data is stored in a relational database using SQL.
 * 3. JSON - Data is stored in a JSON file.
 *
 * The chosen storage method influences various operations such as user
 * registration, login, and role management, ensuring that each operation
 * interacts appropriately with the corresponding storage medium.
 */
public enum AuthStorageType {
    CSV,
    SQL,
    JSON
}
