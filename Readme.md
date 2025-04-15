# TCP-Server with Multi-User Chat Application

A multi-user chat application that allows users to communicate with each other in real-time. The application supports user registration, login, chat messages, and the management of user roles and permissions. It's designed to be easily extendable for adding new features and functionalities.
you can just use TCP server for your own purposes.
## Features

- **User Registration & Login**: Secure registration and login functionality with username, password, email, and phone number, you can implement your authentication system just by extends the authentication class, the default registration system support storing data in json and csv and database you can just go to server settings file and change the auth storage to any type do you want.
--- 
- **Real-Time Messaging**: Users can send and receive messages in real-time.
--- 
- **Role-Based Permissions**: Customizable user roles with permissions for various actions.
---
- **Command System**: Users can execute commands through the chat interface.
--- 
- **Event Handling**: Triggers events on user actions such as chatting or leaving.
--- 
- **Delay Event Handling** triggers events on server & users actions such as check user status or delay broadcast for notify users about something do you want. 
---
- **SSL/TLS Support**: Secure communication between the server and clients using SSL.
---
- **Console Text Color**

## Additional additions
- **Default Commands**: ChangeRoleCommand , BanCommand , HelpCommand ,UserDetailsCommand , OnlineUsersCommand , MuteCommand , NicknameCommand , PrivateMessageCommand.
---
- **Default Events**: AuthenticationEvent , Broadcast , UserChatEvent , UserJoinEvent , UserLeaveEvent , ServerConsoleChatEvent , UserExecuteCommandEvent
---
- **Default Events Handler**: AuthenticationEventHandler , DefaultBroadcastHandler, DefaultChatHandler , DefaultMutedUserHandler , DefaultServerConsoleChatHandler , DefaultUserExecuteCommandHandler , DefaultUserJoinHandler , DefaultUserLeaveHandler

## Installation

To set up and run the multi-user chat application locally, follow these steps:

### Prerequisites

- Java 17 or higher
- SSL certificate for secure communication
- MySQL (if applicable for storing user data)

### Steps

1. **Clone the repository**:

   ```bash
   git clone https://github.com/habbashx/multi-user-chat-app.git
   cd multi-user-chat-app 
    ```
2. **Database setup**:
```sql
CREATE DATABASE tcpserver

use tcpserver


CREATE TABLE users (
userID INT AUTO_INCREMENT PRIMARY KEY,
userIP VARCHAR(45),
userRole VARCHAR(50),
username VARCHAR(50) NOT NULL,
password VARCHAR(255) NOT NULL,
userEmail VARCHAR(100),
phoneNumber VARCHAR(20),
isActiveAccount BOOLEAN DEFAULT TRUE
);
```

3. **Setup the SSL protocol**: 
- 1: Generate a KeyStore:
```bash 
keytool -genkeypair 
  -alias myserver 
  -keyalg RSA 
  -keysize 2048 
  -validity 365 
  -keystore keystore.jks 
  -storepass changeit 
  -keypass changeit 
  -dname "CN=localhost, OU=Dev, O=MyCompany, L=City, S=State, C=US"
```
- 2: Export the certificate from the KeyStore
```bash 
keytool -exportcert 
  -alias myserver 
  -keystore keystore.jks 
  -file myserver.cer 
  -storepass changeit
```
- 3: Create a TrustStore and import the Certificate
```bash 
keytool -importcert 
  -alias myserver 
  -file myserver.cer 
  -keystore truststore.jks 
  -storepass changeit 
  -noprompt
```


