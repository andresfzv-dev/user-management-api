# User Management API

REST API for managing users with role-based access control, built with Java 21 and Spring Boot.

## Tech Stack

- **Java 21**
- **Spring Boot 4.x**
- **Spring Security** — JWT authentication + role-based access control
- **Spring Data JPA** — data persistence
- **MySQL 8** — relational database
- **Docker & Docker Compose** — containerized deployment
- **Maven** — dependency management

## Features

- User CRUD operations
- Role-based authorization (`ADMIN` / `USER`)
- JWT authentication with refresh tokens
- Logout with token invalidation
- Global exception handling with structured error responses
- Input validation

## Getting Started

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

No additional tools required. Java and MySQL run inside Docker containers.

### Installation

1. Clone the repository:
```bash
   git clone https://github.com/andresfzv-dev/user-management-api.git
   cd user-management-api
```

2. Start the application:
```bash
   docker-compose up --build
```

The first build takes a few minutes. Once you see:
```
   Started UserManagementApiApplication in X.XXX seconds
```
The API is available at `http://localhost:8080`.

3. Stop the application:
```bash
   docker-compose down
```

## Environment Variables

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | MySQL JDBC connection URL | `jdbc:mysql://mysql:3306/usermanagement` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `root` |
| `JWT_SECRET` | Secret key for signing JWT tokens | Development default |

> ⚠️ In production, always override `JWT_SECRET` and database credentials with secure values.

## API Endpoints

### Auth

| Method | Endpoint | Description | Auth required |
|---|---|---|---|
| `POST` | `/auth/login` | Login and receive tokens | No |
| `POST` | `/auth/refresh` | Get a new access token | No |
| `POST` | `/auth/logout` | Invalidate refresh token | No |

### Users

| Method | Endpoint | Description | Role required |
|---|---|---|---|
| `POST` | `/api/users` | Create a new user | None |
| `GET` | `/api/users` | Get all users | `ADMIN` |
| `GET` | `/api/users/{id}` | Get user by ID | Authenticated |
| `GET` | `/api/users/me` | Get current user profile | Authenticated |
| `PUT` | `/api/users/{id}` | Update user | `ADMIN` |
| `DELETE` | `/api/users/{id}` | Delete user | `ADMIN` |

## Authentication Flow

1. Create a user via `POST /api/users`
2. Login via `POST /auth/login` — receive an `accessToken` (15 min) and a `refreshToken` (7 days)
3. Use the `accessToken` in the `Authorization` header: `Bearer <token>`
4. When the access token expires, call `POST /auth/refresh` with the refresh token to get a new one
5. Call `POST /auth/logout` to invalidate the session

## Error Responses

All errors follow a consistent structure:
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-03-03T10:25:00",
  "errors": {
    "email": "must be a well-formed email address"
  }
}
```

## Project Structure
```
src/main/java/com/andres/usermanagement/
├── config/          # Security configuration
├── controller/      # REST controllers
├── dto/             # Request and response objects
├── entity/          # JPA entities
├── exception/       # Custom exceptions and global handler
├── repository/      # Spring Data JPA repositories
├── security/        # JWT filter and service
└── service/         # Business logic
```