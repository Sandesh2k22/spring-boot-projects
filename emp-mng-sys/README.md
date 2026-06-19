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

## API Endpoints

### Department

| Method | Endpoint                              | Description                              |
|--------|----------------------------------------|-------------------------------------------|
| POST   | `/api/v1/departments`                  | Create a new department                  |
| GET    | `/api/v1/departments`                  | Get all departments                       |
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
| GET    | `/api/v1/employees`                                | Get all employees                            |
| GET    | `/api/v1/employees?departmentId={id}`              | Get employees filtered by department         |
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
