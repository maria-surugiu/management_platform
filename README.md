# Platform Management API

Backend API for managing users, projects, and tasks, built with Java and Spring Boot.  
The application supports authentication with JWT, role-based authorization, project management, task assignment, and activity logging.

---

# Technologies Used

- **Java 21**
- **Spring Boot 4.0.6**
- **Spring Security**
- **JWT Authentication**
- **Spring Data JPA / Hibernate**
- **PostgreSQL 15**
- **Flyway**
- **Docker & Docker Compose**
- **SLF4J + Logback**

---

# Project Structure

```text
src/main/java/com/personal/management_platform/
├── config/             # Security, JWT, and application configurations
├── controller/         # REST API controllers
├── dto/                # Request and response DTOs
├── exception/          # Custom exceptions and global exception handler
├── model/              # JPA entities and enums
├── repository/         # JPA repositories and specifications
└── service/            # Business logic layer
```

---

# Running the Application

The project is fully containerized using Docker, so you only need:

- Docker Desktop installed and running
- Git (for cloning the repository)
- Postman (for testing the endpoints)

---

## 1. Environment Configuration

1. Clone the repository.

2. Locate the file:

```bash
.env.example
```

3. Rename it to:

```bash
.env
```

4. Open the `.env` file and update the following variable:

```env
JWT_SECRET=
```

Replace it with a secure secret string of at least 64 characters.

Example:

```env
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

The database configuration can remain unchanged.

---

## 2. Start the Application

Open a terminal in the project root (where `docker-compose.yml` is located) and run:

If you make changes to the code and want Docker to rebuild the application:

1. Stop the running containers:

```bash
docker compose down
```

2. Start the application again with rebuild:

```bash
docker compose up --build
```

API Base URL:

```text
http://localhost:8080
```

---

# Testing the API with Postman

A Postman collection is included in the repository.

## Import the Collection

1. Open Postman
2. Click:

```text
Import -> Select Files
```

3. Navigate to:

```text
management_platform/Management Platform.postman_collection.json
```

4. Import the collection

---

# Important: Create the Environment Variable

The collection uses a JWT token stored automatically after login.

Before testing the endpoints, create a Postman Environment variable named:

```text
token_salvat
```

This variable is automatically populated after a successful login request.

The collection is already configured to use this token for authenticated endpoints.

---

# Recommended Testing Flow

To properly test the application functionality, follow the flow below.

---

## 1. Register a User

Endpoint:

```http
POST /api/users/register
```

Example body:

```json
{
  "firstName": "Alex",
  "lastName": "Rosu",
  "email": "alex@example.com",
  "password": "ParolaMea123!"
}
```

This creates a new user account.

---

## 2. Login

Endpoint:

```http
POST /api/users/login
```

Example body:

```json
{
  "email": "alex@example.com",
  "password": "ParolaMea123!"
}
```

After a successful login:

- A JWT token is generated
- The token is automatically saved into the `token_salvat` variable
- All protected endpoints will now work automatically

You remain authenticated until you login with another account.

---

## 3. Check Current User

Endpoint:

```http
GET /api/users/me
```

Use this endpoint to verify:

- authentication works
- the logged-in user
- current user information

---

## 4. Update User Information

Endpoint:

```http
PUT /api/users/me
```

Example:

```json
{
  "firstName": "Alexandru",
  "lastName": "Rosu"
}
```

---

## 5. Change Password

Endpoint:

```http
PUT /api/users/me/password
```

Example:

```json
{
  "currentPassword": "ParolaMea123!",
  "newPassword": "ParolaNoua123!"
}
```

---

# Project Endpoints

## Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users/register` | Register a new user |
| POST | `/api/users/login` | Authenticate user |

---

## User Endpoints

| Method | Endpoint |
|---|---|
| GET | `/api/users/me` |
| PUT | `/api/users/me` |
| PUT | `/api/users/me/password` |
| GET | `/api/users/me/projects` |

---

## Admin Endpoints

| Method | Endpoint |
|---|---|
| GET | `/api/users` |
| PUT | `/api/users/{userId}/role` |
| PUT | `/api/users/{userId}/deactivate` |
| PUT | `/api/users/{userId}/reactivate` |

---

## Project Endpoints

| Method | Endpoint |
|---|---|
| POST | `/api/projects` |
| PUT | `/api/projects/{projectId}` |
| DELETE | `/api/projects/{projectId}` |
| GET | `/api/projects` |
| POST | `/api/projects/{projectId}/members` |

---

## Task Endpoints

| Method | Endpoint |
|---|---|
| POST | `/api/tasks/projects/{projectId}/tasks` |
| PUT | `/api/tasks/{taskId}` |
| PUT | `/api/tasks/{taskId}/assignee` |
| DELETE | `/api/tasks/{taskId}/assignee` |
| DELETE | `/api/tasks/{taskId}` |
| GET | `/api/tasks/projects/{projectId}/tasks?status=IN_PROGRESS` |

---

# Suggested Full Functional Test Flow

A good way to validate the entire application is:

1. Register two users
2. Login with one user
3. Create a project
4. Add the second user to the project
5. Create tasks inside the project
6. Assign tasks to project members
7. Update task status and priority
8. Filter tasks by status

## Testing Admin Endpoints

Admin endpoints require authentication with a user that has the `ADMIN` role.

For security reasons:

- Users cannot register themselves as admins
- No default admin account is automatically created
- The application does not store plain-text passwords, so an admin user cannot easily be inserted manually without generating a valid hashed password

During development, these endpoints were tested by directly modifying the database through the IDE.

Because of this, admin endpoints can only be fully tested if you have direct access to the database to directly change the role of an exiting user from USER to ADMIN

Admin endpoints include:

- Change user role
- Deactivate user
- Reactivate user
- Get all users

This flow validates:

- JWT authentication
- authorization
- role permissions
- project membership
- task assignment
- filtering
- validations
- logging

---

# Logs

The application writes logs inside the Docker container.

## Open Logs from Terminal

Run:

```bash
docker exec -it management_platform-server-1 sh
```

Then:

```bash
cd /app/logs
ls -l
tail -f app.log
```

---

## Download Logs from Docker Desktop

1. Open Docker Desktop
2. Go to:

```text
Containers
```

3. Open:

```text
server-1
```

4. Navigate to:

```text
Files -> app -> logs
```

5. Locate:

```text
app.log
```

6. Right click → Save

---

# Notes

- The API uses JWT Bearer authentication
- Authentication is required for most endpoints
- Some endpoints require ADMIN role permissions
- Database migrations run automatically through Flyway
- Logs are persisted inside the container
