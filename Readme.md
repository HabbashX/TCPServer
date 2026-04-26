# TCPServer

A secure, extensible, multi-client TCP chat server built in Java. It uses SSL/TLS for encrypted communication, supports
multiple authentication storage backends, a role-based permission system, an event-driven architecture, and an
in-game-style command framework.

> **Version:** 1.1.4 &nbsp;|&nbsp; **Java:** 17+ &nbsp;|&nbsp; **Build:** Maven

---

## Table of Contents

- [Features](#features)
- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
    - [1. Clone & Build](#1-clone--build)
    - [2. SSL Keystore Setup](#2-ssl-keystore-setup)
    - [3. Configuration](#3-configuration)
    - [4. Run the Server](#4-run-the-server)
- [Authentication & Storage Backends](#authentication--storage-backends)
- [Roles & Permissions](#roles--permissions)
- [Command System](#command-system)
- [Event System](#event-system)
- [Delay Event System](#delay-event-system)
- [Private Groups](#private-groups)
- [Cooldown System](#cooldown-system)
- [Non-Volatile Permissions](#non-volatile-permissions)
- [Data Directory Layout](#data-directory-layout)
- [Dependencies](#dependencies)
- [Known Issues / Bugs](#known-issues--bugs)
- [Contributing](#contributing)

---

# ✨ New Features

## 📦 Packet Networking System

- Custom binary packet protocol for efficient server communication
- Encoder/Decoder architecture for full extensibility
- Supports multiple packet types (e.g., Text, File)
- Stream-based file transfer using `InputStream`
- Central packet registry for ID-based routing and handling
- Lightweight and optimized for high-throughput networking

---

## ⚡ Packet Registry System

- Centralized registry for all packet codecs
- ID-based mapping for encoding and decoding packets
- Fast lookup system for packet resolution
- Easy extension for new packet types
- Bootstrap initialization system for clean startup

---

## 🧠 Command System Engine

- Annotation-based command registration system
- Automatic alias support for commands
- Full command lifecycle management (execute, validate, log)
- Permission-based execution control
- Built-in cooldown system per user and command
- Supports both synchronous and asynchronous execution
- Virtual-thread-based async execution for high scalability

---

## 🔥 Performance Optimizations

- Virtual threads for lightweight concurrency
- Cached annotation reflection to reduce runtime overhead
- Thread-safe registries using `ConcurrentHashMap`
- Optimized command parsing with minimal allocations
- Reduced object creation in hot execution paths
- Improved overall server throughput under heavy load

---

## 🧩 Configuration System

- Annotation-driven configuration loading per command
- JSON-based configuration support
- Automatic fallback configuration handling
- Cached metadata for faster resolution
- Strict validation for missing or invalid config definitions

---

## 🗂 User Storage System

- Factory-based storage abstraction layer
- Multiple supported storage backends:
    - CSV storage
    - SQL storage
    - JSON storage
- Easy extensibility for custom storage implementations
- Decoupled storage logic from core server system

---

## 🧵 Concurrency & Threading Model

- Fully virtual-thread compatible architecture
- Asynchronous command execution system
- Thread-safe command and packet registries
- Designed for high-concurrency environments
- Non-blocking execution for heavy operations

---

## 📡 Event System Integration

- Command execution event system
- Hooks into command lifecycle
- Supports logging and external integrations
- Extensible event-driven architecture

---

## 🔐 Security & Permissions

- Role-based permission system
- Volatile and persistent permission checks
- Annotation-based access control for commands
- Secure execution validation pipeline

---

## 🧹 System Architecture Improvements

- Modular separation of:
    - Commands
    - Packets
    - Storage
    - Configuration
- Centralized registry patterns
- Reduced system coupling
- Improved maintainability and scalability

---

## 📘 Developer Experience

- Fully documented internal API (Javadoc)
- Simple system initialization flow
- Easy extension system for:
    - Commands
    - Packets
    - Storage providers
- Plugin-like architecture design
- Clean and readable code structure

---

# ⚙️ Architecture Overview

The system is built around modular components:

- **Command Layer** → Handles user input and execution logic
- **Packet Layer** → Manages binary communication protocol
- **Storage Layer** → Abstracts data persistence (CSV / SQL / JSON)
- **Config Layer** → Handles command-specific configuration
- **Event Layer** → Provides lifecycle hooks and integrations

---

# 🚀 Performance Focus

This framework is optimized for:

- High concurrency (virtual threads)
- Low latency command execution
- Fast packet serialization/deserialization
- Minimal runtime reflection overhead

---

# 🧠 Future Extensibility

The architecture is designed to support:

- New packet types without modifying core logic
- New storage backends via factory system
- New commands via annotation system
- Event-driven plugins and extensions

---

## Architecture Overview

```
Client (SSLSocket)
       │
       ▼
 UserHandler (per-user thread)
       │
       ├──► AuthenticationRequest ──► DefaultAuthentication (CSV / JSON / SQL)
       │
       ├──► CommandManager ──► CommandExecutor (BanCommand, MuteCommand, etc.)
       │
       └──► EventManager ──► Listener<UserChatEvent | UserJoinEvent | ...>
                                      │
                              DelayEventManager ──► BroadcastEvent (scheduled)
```

The `Server` class is the entry point. It extends `ServerFoundation` which sets up the SSL socket, thread pool,
event/command managers, and settings injection. Each connected client is assigned a `UserHandler` that runs on its own
thread from the pool.

---

## Project Structure

```
src/main/java/com/habbashx/tcpserver/
├── socket/
│   ├── server/
│   │   ├── Server.java                  # Main server class (singleton)
│   │   ├── foundation/ServerFoundation  # Abstract base: SSL, thread pool, managers
│   │   └── settings/ServerSettings      # @InjectProperty config binding
│   └── client/
│       └── User.java                    # Client-side connection helper
├── connection/
│   ├── UserHandler.java                 # Per-user connection handler
│   ├── handler/ConnectionHandler        # Abstract base for handlers
│   └── console/                         # Server-side console handlers
├── security/
│   ├── Role.java                        # Role enum (DEFAULT → SUPER_ADMINISTRATOR)
│   ├── Permission.java                  # Permission constants
│   ├── auth/DefaultAuthentication       # Register/login logic
│   └── container/NonVolatilePermissionContainer  # Persistent per-user permissions
├── command/
│   ├── CommandExecutor.java             # Base class for all commands
│   ├── manager/CommandManager           # Register & execute commands
│   └── defaultcommand/                  # Built-in commands (ban, mute, etc.)
├── event/
│   ├── Event.java                       # Base event class
│   ├── manager/EventManager             # Register & trigger events
│   └── (UserChatEvent, UserJoinEvent, AuthenticationEvent, ...)
├── delayevent/
│   ├── manager/DelayEventManager        # Scheduled event dispatcher
│   └── BroadcastEvent.java
├── listener/
│   ├── Listener.java                    # Event listener interface
│   ├── DelayListener.java               # Delay event listener interface
│   └── handler/                         # Default built-in listeners
├── cooldown/
│   └── CooldownManager.java             # Per-user cooldown tracking
├── logger/
│   └── ServerLogger.java                # Colored console logger
└── user/
    └── UserDetails.java                 # User data model (builder pattern)
```

---

## Requirements

| Tool                 | Version                            |
|----------------------|------------------------------------|
| Java                 | 17 or higher                       |
| Maven                | 3.6+                               |
| MySQL                | 8.0+ *(only if using SQL storage)* |
| A valid SSL keystore | `.jks` or `.p12`                   |

---

## Getting Started

### 1. Clone & Build

```bash
git clone https://github.com/habbashx/TCPServer.git
cd TCPServer
mvn clean package
```

### 2. SSL Keystore Setup

The server requires an SSL keystore for encrypted connections. Generate a self-signed one for development:

```bash
keytool -genkeypair \
  -alias server \
  -keyalg RSA \
  -keysize 2048 \
  -validity 365 \
  -keystore keystore.jks \
  -storepass yourpassword \
  -dname "CN=localhost, OU=Dev, O=YourOrg, L=City, ST=State, C=US"
```

Place the generated `keystore.jks` somewhere accessible and note its path.

### 3. Configuration

The server reads settings from a properties file injected via the `@InjectProperty` / `@Settings` annotation system.
Create a `server.properties` (or the file your `@Settings` points to) with the following:

```properties
# Network
server.settings.host=localhost
server.settings.port=8443
server.settings.reusableAddress=true
# SSL
server.settings.security.keystore.path=/path/to/keystore.jks
server.settings.security.keystore.password=yourpassword
server.settings.security.truststore.path=/path/to/truststore.jks
server.settings.security.truststore.password=yourpassword
# Auth
server.settings.auth.storage.type=SQL
# Options: CSV | JSON | SQL
# Chat cooldown (seconds)
server.settings.user.chat.cooldown=3
# Database (only needed for SQL storage)
server.settings.database.url=jdbc:mysql://localhost:3306/tcpserver
server.settings.database.username=root
server.settings.database.password=yourdbpassword
```

#### Database setup (SQL only)

```sql
CREATE
DATABASE tcpserver;
USE
tcpserver;

CREATE TABLE users
(
    userID          VARCHAR(36) PRIMARY KEY,
    userIP          VARCHAR(50),
    userRole        VARCHAR(30),
    username        VARCHAR(50) UNIQUE NOT NULL,
    password        VARCHAR(255)       NOT NULL,
    userEmail       VARCHAR(100),
    phoneNumber     VARCHAR(20),
    isActiveAccount BOOLEAN DEFAULT TRUE
);
```

### 4. Run the Server

```bash
java -jar target/tcp-server-1.1.4.jar
```

Or from your IDE run `Server.main()`.

---

## Authentication & Storage Backends

Set `server.settings.auth.storage.type` to one of:

| Value  | Description           | File                       |
|--------|-----------------------|----------------------------|
| `CSV`  | Flat-file CSV storage | `data/users.csv`           |
| `JSON` | JSON array file       | `data/users.json`          |
| `SQL`  | MySQL database        | Configured via DB settings |

All passwords are hashed with **BCrypt** before storage. Plain-text passwords are never written to disk.

### Registration flow

1. Client connects via SSL
2. Server prompts: `register` or `login`
3. For `register`: collects username, password, email, phone number
4. Validates: no symbols in username, valid phone, valid email
5. Hashes password with BCrypt and saves to the configured backend
6. `AuthenticationEvent` is fired → `AuthenticationEventHandler` → `UserJoinEvent`

---

## Roles & Permissions

There are 5 built-in roles in ascending order of privilege:

| Role                  | Prefix                  |
|-----------------------|-------------------------|
| `DEFAULT`             | *(none)*                |
| `MODERATOR`           | `[Moderator]`           |
| `OPERATOR`            | `[Operator]`            |
| `ADMINISTRATOR`       | `[Administrator]`       |
| `SUPER_ADMINISTRATOR` | `[Super-Administrator]` |

Each role has a predefined set of permission integers loaded from configuration. Admins can also grant **volatile
permissions** (session-only) or **non-volatile permissions** (persisted to JSON) to individual users.

### Permission constants

| Constant                             | Value    | Purpose              |
|--------------------------------------|----------|----------------------|
| `NO_PERMISSION_REQUIRED`             | `0x00`   | Any user can run     |
| `BAN_PERMISSION`                     | `0x01`   | Ban users            |
| `UN_BAN_PERMISSION`                  | `0x02`   | Unban users          |
| `MUTE_PERMISSION`                    | `0x03`   | Mute users           |
| `UN_MUTE_PERMISSION`                 | `0x04`   | Unmute users         |
| `CHANGE_ROLE_PERMISSION`             | `0x05`   | Change a user's role |
| `NICKNAME_PERMISSION`                | `0x06`   | Set a nickname       |
| `ADD_NEW_PERMISSIONS_PERMISSION`     | `0x08`   | Grant permissions    |
| `REMOVE_PERMISSIONS_PERMISSION`      | `0x09`   | Revoke permissions   |
| `CHECK_PERMISSIONS_PERMISSION`       | `0x0A`   | View permissions     |
| `RETRIEVES_WRITTEN_BYTES_PERMISSION` | `0x0B`   | View byte stats      |
| `NO_PERMISSION`                      | `0x0EFA` | Super admin bypass   |

---

## Command System

Commands are classes that extend `CommandExecutor` and are annotated with `@Command`:

```java

@Command(
        name = "hello",
        aliases = {"hi"},
        description = "Say hello",
        permission = Permission.NO_PERMISSION_REQUIRED,
        cooldownTime = 5L,
        cooldownTimeUnit = TimeUnit.SECONDS,
        executionLog = true,
        isAsync = false
)
public class HelloCommand extends CommandExecutor {

    @Override
    public void execute(CommandContext context) {
        if (context.getSender() instanceof UserHandler user) {
            user.sendMessage("Hello, " + user.getUserDetails().getUsername() + "!");
        }
    }
}
```

Register in `Server`:

```java
commandManager.registerCommand(new HelloCommand());
```

Users trigger commands by typing `/hello` in the chat.

### Built-in commands

| Command                     | Aliases           | Permission  | Description              |
|-----------------------------|-------------------|-------------|--------------------------|
| `/ban <user>`               | `banned`, `block` | BAN         | Ban a user               |
| `/unban <user>`             | —                 | UN_BAN      | Unban a user             |
| `/mute <user>`              | —                 | MUTE        | Mute a user              |
| `/unmute <user>`            | —                 | UN_MUTE     | Unmute a user            |
| `/changerole <user> <role>` | —                 | CHANGE_ROLE | Change user role         |
| `/nick <nickname>`          | —                 | NICKNAME    | Set a nickname           |
| `/pm <user> <msg>`          | —                 | none        | Private message          |
| `/list`                     | —                 | none        | List online users        |
| `/userdetails <user>`       | —                 | none        | View user info           |
| `/info`                     | —                 | none        | Server info              |
| `/help`                     | —                 | none        | List commands            |
| `/memory`                   | —                 | none        | JVM memory stats         |
| `/addperm <user> <perm>`    | —                 | ADD_PERM    | Add permission           |
| `/removeperm <user> <perm>` | —                 | REMOVE_PERM | Remove permission        |
| `/checkperm <user>`         | —                 | CHECK_PERM  | View permissions         |
| `/bytes`                    | —                 | BYTES       | Total bytes written      |
| `/group <subcommand>`       | —                 | none        | Private group management |

---

## Event System

Register listeners for any server event:

```java

@EventHandler(priority = Priority.HIGH)
public class MyJoinListener implements Listener<UserJoinEvent> {

    @Override
    public void onEvent(UserJoinEvent event) {
        event.getUserHandler().sendMessage("Welcome to the server!");
    }
}
```

Then register it:

```java
server.getEventManager().

registerEvent(new MyJoinListener());
```

### Built-in events

| Event                     | Fired when                          |
|---------------------------|-------------------------------------|
| `UserJoinEvent`           | User authenticates successfully     |
| `UserLeaveEvent`          | User disconnects                    |
| `UserChatEvent`           | User sends a message                |
| `UserExecuteCommandEvent` | User runs a command                 |
| `AuthenticationEvent`     | Register or login attempt completes |
| `ServerConsoleChatEvent`  | Server console sends a message      |
| `PrivateGroupChatEvent`   | Message sent in a private group     |

### Event handler priorities

`Priority.HIGH` → `NORMAL` → `LOW` (higher priority listeners execute first)

Set `isAsync = true` in `@EventHandler` to run the listener off the main thread.

---

## Delay Event System

For recurring scheduled broadcasts or other periodic events:

```java

@DelayEventHandler(delayMilliSeconds = 10000, priority = Priority.LOW)
public class MyBroadcastHandler implements DelayListener<BroadcastEvent> {

    @Override
    public void onEvent(BroadcastEvent event) {
        // fires every 10 seconds
    }
}
```

Register with:

```java
server.getDelayEventManager().

registerEvent(new MyBroadcastHandler());
```

---

## Private Groups

Users can create private chat rooms with the `/group` command:

| Subcommand              | Description                            |
|-------------------------|----------------------------------------|
| `/group CREATE`         | Create a new group (you become owner)  |
| `/group JOIN <id>`      | Join a group by its ID                 |
| `/group LEAVE`          | Leave the current group                |
| `/group DELETE`         | Delete your group                      |
| `/group SEND <message>` | Send a message to your group           |
| `/group INFO`           | Show group ID, owner, and member count |

Groups are identified by a random 8-digit ID. Only the group owner can delete it.

---

## Cooldown System

`CooldownManager` handles rate-limiting per user. Each command and `UserChatEvent` has its own cooldown instance.

```java
CooldownManager cm = new CooldownManager(5); // 5 seconds
cm.

setCooldown("alice");
cm.

isOnCooldown("alice");  // true
cm.

getRemainingTime("alice"); // seconds left
cm.

removeCooldown("alice");
```

Chat cooldown is configured globally via `server.settings.user.chat.cooldown`. Users with permission `0x07` bypass chat
cooldown.

---

## Non-Volatile Permissions

Permissions granted with `/addperm` are stored permanently in:

```
containers/permissions/usersPermissions.json
```

Format:

```json
[
  {
    "userID": "abc123",
    "permissions": [
      1,
      3,
      8
    ]
  }
]
```

These persist across server restarts and are loaded when the user connects.

---

## Data Directory Layout

```
data/
├── users.csv          # Users (if CSV storage)
├── users.json         # Users (if JSON storage)
├── bannedUsers.csv    # Banned usernames
└── mutedUsers.csv     # Muted usernames

containers/
└── permissions/
    └── usersPermissions.json   # Non-volatile per-user permissions
```

The server creates `users.csv` / `users.json` automatically on first registration if they don't exist.

---

## Dependencies

| Library                 | Version | Purpose                     |
|-------------------------|---------|-----------------------------|
| `jetbrains/annotations` | 24.0.1  | `@NotNull`, `@Nullable`     |
| `snakeyaml`             | 2.2     | YAML config parsing         |
| `commons-csv`           | 1.10.0  | CSV read/write              |
| `jbcrypt`               | 0.4     | BCrypt password hashing     |
| `jackson-databind`      | 2.15.2  | JSON serialization          |
| `mysql-connector-java`  | 8.0.33  | MySQL JDBC driver           |
| `property-parser`       | 1.1.1   | `@InjectProperty` injection |

---

## Known Issues / Bugs

The following bugs have been identified and are pending fixes:

| #  | Location                                   | Issue                                                                                      |
|----|--------------------------------------------|--------------------------------------------------------------------------------------------|
| 1  | `UserHandler`                              | `hasVolatilePermission()` always checks permission `0` instead of the given value          |
| 2  | `DefaultAuthentication`                    | CSV register saves wrong IP/ID (uses static util instead of socket values)                 |
| 3  | `DefaultAuthentication`                    | Invalid email doesn't stop registration — missing `return`                                 |
| 4  | `Server.PrivateGroup`                      | `removeUser()` throws `ConcurrentModificationException`                                    |
| 5  | `PrivateGroupCommand`                      | `/group JOIN` with no ID crashes with `IndexOutOfBoundsException`                          |
| 6  | `Server`                                   | `main()` creates `new Server()` instead of calling `getInstance()`                         |
| 7  | `BanCommandManager` / `MuteCommandManager` | File overwritten on each ban/mute — should append                                          |
| 8  | `BanCommandManager`                        | `unBanUser()` modifies set during iteration — `ConcurrentModificationException`            |
| 9  | `DelayEventManager`                        | `scheduleAtFixedRate` inside `triggerEvent` spawns a new task every call — CPU/memory leak |
| 10 | `EventManager`                             | Warning message missing `.formatted()` args — prints literal `%s`                          |
| 11 | `AuthenticationEventHandler`               | Missing `RESET` after failed register message                                              |
| 12 | `UserChatEvent`                            | `equals()` / `hashCode()` NPE when `message` is null                                       |
| 13 | `CommandManager`                           | Cooldown time unit conversion is inverted                                                  |

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m "Add my feature"`
4. Push: `git push origin feature/my-feature`
5. Open a Pull Request

Please follow the existing code style and annotate all commands with `@Command` and all event listeners with
`@EventHandler`.

---

*Built by [@habbashx](https://github.com/habbashx)*


