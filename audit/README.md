# Audit System

A Spring Boot REST application that manages users and automatically records an
**audit trail** of every create, update, and delete operation. Each change is
persisted to a dedicated `audit_log` table and can be queried back through a set
of REST endpoints.

## Features

- User CRUD API (`/api/users`)
- Automatic audit logging on user **create / update / delete**
- Each audit entry captures: action type, entity name, entity ID, the user who
  made the change, a timestamp, and a JSON snapshot of the old and new values
- Audit query API (`/api/audit`) to fetch records by entity, action type, user,
  date range, or to get the latest change for an entity
- Interactive API docs via Swagger UI

## Tech Stack

| Component | Version |
|-----------|---------|
| Java | 17 |
| Spring Boot | 3.5.15 |
| Spring Data JPA | (managed by Boot) |
| MySQL | 8.x |
| springdoc-openapi (Swagger UI) | 2.8.16 |
| Lombok | (managed by Boot) |
| Build tool | Maven (wrapper included) |

## Project Structure

```
src/main/java/com/audit
├── AuditApplication.java          # Spring Boot entry point
├── controller
│   ├── UserController.java        # /api/users  – user CRUD
│   └── AuditController.java       # /api/audit  – audit queries
├── service
│   ├── UserService.java           # user logic; triggers audit on each change
│   └── AuditService.java          # builds & persists audit entries
├── repository
│   ├── UserRepository.java
│   └── AuditLogRepository.java
└── entity
    ├── User.java                  # users table
    └── AuditLog.java              # audit_log table
src/main/resources
└── application.yaml               # DB & JPA configuration
```

## Prerequisites

- JDK 17+
- MySQL running on `localhost:3306`
- A database and user matching `application.yaml`

## Database Setup

Create the database and user expected by the app:

```sql
CREATE DATABASE audit_db;
CREATE USER 'springuser'@'localhost' IDENTIFIED BY '123';
GRANT ALL PRIVILEGES ON audit_db.* TO 'springuser'@'localhost';
FLUSH PRIVILEGES;
```

Tables are created/updated automatically on startup (`spring.jpa.hibernate.ddl-auto=update`).

### Configuration

Defaults live in [`src/main/resources/application.yaml`](src/main/resources/application.yaml):

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/audit_db
    username: springuser
    password: 123
server:
  port: 8080
```

Override credentials for real environments via environment variables or
external config rather than committing them.

## Build & Run

```bash
# Build (skip tests)
./mvnw clean package -DskipTests

# Run the packaged jar
java -jar target/audit-0.0.1-SNAPSHOT.jar

# …or run directly with Maven
./mvnw spring-boot:run
```

The app starts on **http://localhost:8080**. Wait for the log line
`Started AuditApplication`.

## API Documentation

Once running, open Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

## API Reference

### User API — `/api/users`

| Method | Path | Description | Header |
|--------|------|-------------|--------|
| POST | `/api/users` | Create a user | `X-Changed-By` (defaults to `SYSTEM`) |
| GET | `/api/users` | List all users | — |
| GET | `/api/users/{id}` | Get user by ID | — |
| PUT | `/api/users/{id}` | Update a user | `X-Changed-By` |
| DELETE | `/api/users/{id}` | Delete a user | `X-Changed-By` |

> The optional `X-Changed-By` header records **who** made the change in the
> audit log. If omitted, it defaults to `SYSTEM`.

### Audit API — `/api/audit`

| Method | Path | Query params |
|--------|------|--------------|
| GET | `/api/audit/entity` | `entityName`, `entityId` |
| GET | `/api/audit/action` | `actionType` (`CREATE`/`UPDATE`/`DELETE`) |
| GET | `/api/audit/user` | `changedBy` |
| GET | `/api/audit/date-range` | `startDate`, `endDate` (ISO date-time) |
| GET | `/api/audit/entity-date-range` | `entityName`, `entityId`, `startDate`, `endDate` |
| GET | `/api/audit/latest` | `entityName`, `entityId` |

> **Note:** `entityName` is the entity **type**, which is `User` for this
> application — not the username. For example, "the User with id 5" is queried
> with `entityName=User&entityId=5`.

## Audit Log Fields

| Column | Description |
|--------|-------------|
| `action_type` | `CREATE`, `UPDATE`, or `DELETE` |
| `entity_name` | The entity type (e.g. `User`) |
| `entity_id` | ID of the affected entity |
| `changed_by` | User who performed the change |
| `timestamp` | When the change occurred |
| `old_values` | JSON snapshot before the change (null for CREATE) |
| `new_values` | JSON snapshot after the change (null for DELETE) |
| `description` | Optional free-text note |

## Example Usage

```bash
# 1. Create a user (recorded as CREATE by "alice")
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "X-Changed-By: alice" \
  -d '{"username":"jdoe","email":"jdoe@example.com","firstName":"John","lastName":"Doe"}'

# 2. Update the user (recorded as UPDATE by "bob") — use the id from step 1
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -H "X-Changed-By: bob" \
  -d '{"username":"jdoe","email":"new@example.com","firstName":"John","lastName":"Doe","isActive":true}'

# 3. Delete the user (recorded as DELETE by "carol")
curl -X DELETE http://localhost:8080/api/users/1 -H "X-Changed-By: carol"

# 4. Fetch the full audit trail for that user
curl "http://localhost:8080/api/audit/entity?entityName=User&entityId=1"

# 5. Or look it up other ways
curl "http://localhost:8080/api/audit/action?actionType=CREATE"
curl "http://localhost:8080/api/audit/user?changedBy=alice"
```

### Verify directly in the database

```bash
mysql -u springuser -p123 audit_db -e "SELECT id, action_type, entity_name, entity_id, changed_by, timestamp FROM audit_log;"
```

## Troubleshooting

- **Audit query returns `[]`** — Check that `entityName` is `User` (the entity
  type), not the username, and that `entityId` is a real number. Also confirm
  the app is actually running: `curl -s -o /dev/null -w "%{http_code}" localhost:8080/api/users` should print `200`.
- **`Port 8080 was already in use`** — A previous instance is still running.
  Stop it: `kill -9 $(lsof -ti:8080)`.
- **App fails to start on the database** — Ensure MySQL is up and the
  `audit_db` database and `springuser` credentials exist (see Database Setup).