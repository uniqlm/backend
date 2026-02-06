# UniqLM Backend API 🚀

This is the core authentication and user management service for the **UniqLM** project. Built with Spring Boot 3, it provides a secure, stateless REST API using JWT and integrates Google OAuth2.

---

## ⚙️ Setup & Installation

### 1. Prerequisites
- **Java 17** installed.
- **PostgreSQL** running.
- **Maven** 3.8+ installed.

### 2. Database Setup
Create a database named `uniq_dev` in your PostgreSQL instance:
```sql
CREATE DATABASE uniq_dev;

```

### 3. Google OAuth Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/).
2. Create a project and OAuth 2.0 Client ID for "Web Application".
3. Add Authorized Redirect URI: `http://localhost:9090/login/oauth2/code/google`.

### 4. Configuration

Edit `src/main/resources/application.properties`:

```properties
server.port=9090

# Database Configuration
spring.datasource.url=jdbc:postgresql://172.30.7.104:5432/uniq_dev
spring.datasource.username=postgres
spring.datasource.password=@EPCd3vt34m
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Google OAuth Configuration
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=profile,email

```

---

## 🏃 How to Run

### Using Maven

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

```

The server will be live at: `http://localhost:9090`

---

## 📍 API Documentation & Sample CURLs

### 1. User Registration (Public)

Creates a new account and hashes the password using BCrypt.

```bash
curl --location 'http://localhost:9090/api/auth/register' \
--header 'Content-Type: application/json' \
--data '{
    "username": "syafrin",
    "password": "mypassword123",
    "name": "Syafrin",
    "email": "syafrin@example.com",
    "phone1": "08123456789"
}'

```

### 2. Login (Public)

Exchange credentials for a JWT Token.

```bash
curl --location 'http://localhost:9090/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
    "username": "syafrin",
    "password": "mypassword123"
}'

```

**Success Response:** `{"token": "eyJhbG..."}`

### 3. Get User Profile (Protected)

Requires a valid JWT in the Authorization header.

```bash
curl --location 'http://localhost:9090/api/auth/profile' \
--header 'Authorization: Bearer <YOUR_JWT_TOKEN_HERE>'

```

### 4. Update Profile (Protected)

Updates details for the authenticated user.

```bash
curl --location --request PUT 'http://localhost:9090/api/auth/update' \
--header 'Authorization: Bearer <YOUR_JWT_TOKEN_HERE>' \
--header 'Content-Type: application/json' \
--data '{
    "username": "syafrin",
    "name": "Syafrin Updated",
    "email": "newemail@example.com",
    "phone1": "0899999999"
}'

```

### 5. Google OAuth Login (Browser)

To initiate the Google sign-in flow, navigate to this URL in a web browser:
`http://localhost:9090/oauth2/authorization/google`

---

## 🔐 Security Architecture

* **JWT:** Tokens are generated upon successful login or Google OAuth success.
* **Stateless:** The server does not store session data in memory. The frontend must store the JWT and send it in the `Authorization: Bearer <token>` header.
* **CORS:** Configured to allow requests from `http://localhost:3000` (React) and `http://localhost:5173` (Vite).

---

## 📝 Database Schema (`mst_user`)

| Column | Type | Description |
| --- | --- | --- |
| id | BIGINT | Primary Key (Identity) |
| username | VARCHAR | Unique user identifier |
| password | VARCHAR | BCrypt Hashed password |
| email | VARCHAR | Registered email address |
| name | VARCHAR | User's full name |
| phone_1 | VARCHAR | Primary contact number |
| phone_2 | VARCHAR | Secondary contact number |

```

**Would you like me to generate a Dockerfile for this configuration next?**

```
