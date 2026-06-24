# Employee Management System

A Spring Boot REST API for managing **Employees** and **Departments**, built with Spring Data JPA, MySQL, and Maven.
It demonstrates a clean, layered architecture with the DTO pattern, Bean Validation, and centralized exception handling.

## Features

- Full CRUD for `Department` and `Employee`
- One-to-Many relationship: a `Department` has many `Employee`s
- Request validation via Jakarta Bean Validation
- Centralized error handling via `@ControllerAdvice`
- Consistent JSON response envelope (`ApiResponse<T>`) with meaningful messages
- Layered architecture: Controller → Service → Repository, with separate DTO/Entity/Mapper layers

## Entity Relationship

```
┌────────────────────────┐         1        N ┌────────────────────────┐
│       Department       │◄───────────────────│        Employee        │
├────────────────────────┤  department_id (FK) ├────────────────────────┤
│ id (PK)                │                     │ id (PK)                │
│ name        [unique]   │                     │ firstName              │
│ description            │                     │ lastName               │
└────────────────────────┘                     │ email       [unique]   │
                                                │ phoneNumber            │
                                                │ dateOfJoining          │
                                                │ salary                 │
                                                │ department_id (FK)     │
                                                └────────────────────────┘
```

- **Department → Employee**: one-to-many, mapped by `Employee.department`. `Department.employees` uses `cascade = ALL` + `orphanRemoval = true`, so deleting a department deletes its employees, and removing an employee from the in-memory list deletes it from the database.
- **Employee → Department**: many-to-one via `department_id` (`nullable = false`, `FetchType.LAZY`) — every employee must belong to exactly one department.
- Both relationship fields are excluded from Lombok's `equals()`/`hashCode()`/`toString()` (`@EqualsAndHashCode.Exclude`, `@ToString.Exclude`) to avoid infinite recursion across the bidirectional link.
- API responses never serialize entities directly — `DepartmentResponseDto` embeds a lightweight `EmployeeSummaryDto` list, and `EmployeeResponseDto` embeds just `departmentId`/`departmentName`, so there's no circular JSON either.

## Tech Stack

| Layer       | Technology                          |
|-------------|--------------------------------------|
| Language    | Java 17                              |
| Framework   | Spring Boot 3.2.5                    |
| Persistence | Spring Data JPA (Hibernate)          |
| Database    | MySQL 8                              |
| Build Tool  | Maven                                |
| Validation  | Jakarta Bean Validation (Hibernate Validator) |
| Utilities   | Lombok                               |

## Project Structure

```
emp-mng-sys/
├── pom.xml
├── README.md
├── SKILLS.md
└── src
    ├── main
    │   ├── java/com/empmngsys/empmngsys
    │   │   ├── EmpMngSysApplication.java        # Application entry point
    │   │   ├── controller/                      # REST controllers
    │   │   │   ├── DepartmentController.java
    │   │   │   └── EmployeeController.java
    │   │   ├── service/                         # Service interfaces
    │   │   │   ├── DepartmentService.java
    │   │   │   ├── EmployeeService.java
    │   │   │   └── impl/                        # Service implementations
    │   │   │       ├── DepartmentServiceImpl.java
    │   │   │       └── EmployeeServiceImpl.java
    │   │   ├── repository/                      # Spring Data JPA repositories
    │   │   │   ├── DepartmentRepository.java
    │   │   │   └── EmployeeRepository.java
    │   │   ├── entity/                           # JPA entities
    │   │   │   ├── Department.java
    │   │   │   └── Employee.java
    │   │   ├── dto/                              # Request/response DTOs
    │   │   │   ├── ApiResponse.java
    │   │   │   ├── DepartmentRequestDto.java
    │   │   │   ├── DepartmentResponseDto.java
    │   │   │   ├── EmployeeRequestDto.java
    │   │   │   ├── EmployeeResponseDto.java
    │   │   │   └── EmployeeSummaryDto.java
    │   │   ├── mapper/                           # Entity <-> DTO mapping
    │   │   │   ├── DepartmentMapper.java
    │   │   │   └── EmployeeMapper.java
    │   │   └── exception/                        # Global exception handling
    │   │       ├── GlobalExceptionHandler.java
    │   │       ├── ErrorResponse.java
    │   │       ├── ResourceNotFoundException.java
    │   │       └── DuplicateResourceException.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/empmngsys/empmngsys/
```

## Prerequisites

- JDK 17+
- Maven 3.6+
- MySQL 8+ running locally (or accessible remotely)

## Setup

1. **Create the database** (optional — the app can auto-create it via `createDatabaseIfNotExist=true`):

   ```sql
   CREATE DATABASE emp_mng_sys;
   ```

2. **Configure database credentials.** Either edit `src/main/resources/application.yml`, or set environment variables (recommended):

   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=your_password
   ```

3. **Build the project:**

   ```bash
   mvn clean install
   ```

4. **Run the application:**

   ```bash
   mvn spring-boot:run
   ```

   The API will be available at `http://localhost:8080`.

## Security

All endpoints are secured with **Spring Security** using **stateless JWT authentication**.
Clients authenticate once at `POST /api/v1/auth/login` with username/password and
receive a signed JWT, which they then send as a `Bearer` token on every request.
Passwords are hashed with **BCrypt** (`BCryptPasswordEncoder`) at startup; tokens are
signed with HMAC-SHA.

### Roles

| Operation                | Required role        |
|--------------------------|----------------------|
| `GET` (reads)            | `USER` or `ADMIN`    |
| `POST`/`PUT`/`DELETE`    | `ADMIN`              |

Unauthenticated requests receive `401 Unauthorized`; authenticated requests
without the required role receive `403 Forbidden`.

### Default users

Credentials are configurable via environment variables (defaults shown):

| Username | Password   | Roles         | Env overrides                                  |
|----------|------------|---------------|------------------------------------------------|
| `user`   | `user123`  | USER          | `APP_USER_USERNAME`, `APP_USER_PASSWORD`       |
| `admin`  | `admin123` | USER, ADMIN   | `APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD`     |

> Change these before deploying to any non-local environment.

### JWT settings

| Setting        | Default  | Env override            |
|----------------|----------|-------------------------|
| Signing secret | dev key  | `APP_JWT_SECRET` (Base64, ≥ 256 bits) |
| Token lifetime | 1 hour   | `APP_JWT_EXPIRATION_MS` |

### Authenticating

**1. Log in to get a token:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
Response (`data.token` holds the JWT):
```json
{
  "success": true,
  "message": "Authentication successful",
  "data": { "token": "eyJhbGci...", "tokenType": "Bearer", "expiresInMs": 3600000 }
}
```

**2. Call protected endpoints with the token:**
```bash
TOKEN="eyJhbGci..."

# Read (any authenticated user)
curl http://localhost:8080/api/v1/employees -H "Authorization: Bearer $TOKEN"

# Write (ADMIN only)
curl -X DELETE http://localhost:8080/api/v1/employees/1 -H "Authorization: Bearer $TOKEN"

# No/invalid token -> 401
curl -i http://localhost:8080/api/v1/employees
```

In Postman: call `POST /api/v1/auth/login`, copy `data.token`, then on other
requests set **Authorization → Type: Bearer Token** and paste it.

## API Endpoints

### Auth

| Method | Endpoint              | Description                          |
|--------|-----------------------|--------------------------------------|
| POST   | `/api/v1/auth/login`  | Authenticate and obtain a JWT (public) |

### Department

| Method | Endpoint                              | Description                              |
|--------|----------------------------------------|-------------------------------------------|
| POST   | `/api/v1/departments`                  | Create a new department                  |
| GET    | `/api/v1/departments?page=&size=&sort=`| Get all departments (paginated)           |
| GET    | `/api/v1/departments/{id}`             | Get a department by ID                    |
| PUT    | `/api/v1/departments/{id}`             | Update a department                       |
| DELETE | `/api/v1/departments/{id}`             | Delete a department (cascades employees)  |

**Sample request body** (`POST /api/v1/departments`):
```json
{
  "name": "Engineering",
  "description": "Builds and maintains the product"
}
```

### Employee

| Method | Endpoint                                          | Description                                  |
|--------|----------------------------------------------------|-----------------------------------------------|
| POST   | `/api/v1/employees`                                | Create a new employee                        |
| GET    | `/api/v1/employees?page=&size=&sort=`              | Get all employees (paginated)                |
| GET    | `/api/v1/employees?departmentId={id}&page=&size=`  | Get employees filtered by department (paginated) |
| GET    | `/api/v1/employees/{id}`                           | Get an employee by ID                        |
| PUT    | `/api/v1/employees/{id}`                           | Update an employee                           |
| DELETE | `/api/v1/employees/{id}`                           | Delete an employee                           |

**Sample request body** (`POST /api/v1/employees`):
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@example.com",
  "phoneNumber": "+15551234567",
  "dateOfJoining": "2024-01-15",
  "salary": 75000.00,
  "departmentId": 1
}
```

### Pagination

Both list endpoints (`GET /api/v1/departments`, `GET /api/v1/employees`) accept Spring Data's standard paging query parameters:

| Param  | Default | Notes                                                              |
|--------|---------|---------------------------------------------------------------------|
| `page` | `0`     | Zero-indexed page number                                            |
| `size` | `10`    | Capped at `100` (`spring.data.web.pageable.max-page-size`)          |
| `sort` | `id,asc`| e.g. `sort=lastName,desc`; repeat the param for multi-field sorting |

`data` is a Spring `Page` object instead of a plain array:
```json
{
  "success": true,
  "message": "Employees retrieved successfully",
  "data": {
    "content": [ { } ],
    "number": 0,
    "size": 10,
    "totalElements": 6,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-06-20T23:11:06.614346749"
}
```

### Response Envelope

All successful responses follow this shape:
```json
{
  "success": true,
  "message": "Employee created successfully",
  "data": { },
  "timestamp": "2026-06-18T10:00:00"
}
```

All errors (validation, not found, conflicts, etc.) follow this shape:
```json
{
  "success": false,
  "status": 404,
  "message": "Employee not found with id: 42",
  "timestamp": "2026-06-18T10:00:00"
}
```

Validation errors additionally include a field-level `errors` map:
```json
{
  "success": false,
  "status": 400,
  "message": "Validation failed for one or more fields",
  "errors": {
    "email": "Email must be a valid email address"
  },
  "timestamp": "2026-06-18T10:00:00"
}
```

## Error Handling

Handled centrally in `GlobalExceptionHandler`:

| Exception                              | HTTP Status              |
|------------------------------------------|---------------------------|
| `ResourceNotFoundException`             | 404 Not Found             |
| `DuplicateResourceException`            | 409 Conflict              |
| `MethodArgumentNotValidException`       | 400 Bad Request           |
| `MethodArgumentTypeMismatchException`   | 400 Bad Request           |
| `HttpMessageNotReadableException`       | 400 Bad Request           |
| Any other `Exception`                   | 500 Internal Server Error |
