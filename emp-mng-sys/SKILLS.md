# Skills & Concepts Demonstrated

This document lists the technologies, concepts, and design practices used to build the Employee Management System.

## Languages & Build Tools

- **Java 17** — records-free, modern Java syntax (`var`, `Stream.toList()`)
- **Maven** — dependency management and build lifecycle (`spring-boot-starter-parent`)

## Frameworks & Libraries

- **Spring Boot 3.2.5** — application bootstrapping and auto-configuration
- **Spring Web (Spring MVC)** — REST controllers, `@RestController`, `@RequestMapping`
- **Spring Data JPA** — repository abstraction over Hibernate (`JpaRepository`)
- **Hibernate / JPA (Jakarta Persistence)** — ORM, entity mapping, lazy loading, cascading
- **Jakarta Bean Validation (Hibernate Validator)** — declarative input validation (`@NotBlank`, `@Email`, `@DecimalMin`, etc.)
- **Lombok** — boilerplate reduction (`@Data`, `@RequiredArgsConstructor`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- **MySQL Connector/J** — JDBC driver for MySQL
- **Jackson** — JSON serialization/deserialization, `@JsonInclude` for response shaping

## Architectural Patterns

- **Layered Architecture** — strict separation of Controller, Service, Repository, Entity, and DTO layers
- **DTO Pattern** — request/response DTOs decoupled from JPA entities (`EmployeeRequestDto`, `EmployeeResponseDto`, etc.)
- **Mapper Pattern** — explicit static mapper classes (`EmployeeMapper`, `DepartmentMapper`) instead of exposing entities directly
- **Service Interface + Implementation** — `DepartmentService`/`EmployeeService` interfaces with `impl` package implementations, enabling testability and loose coupling
- **Repository Pattern** — Spring Data JPA repositories abstracting persistence logic
- **Global Exception Handling** — centralized `@ControllerAdvice` + `@ExceptionHandler` for consistent error responses
- **Response Wrapper Pattern** — uniform `ApiResponse<T>` envelope for all success responses and `ErrorResponse` for failures

## JPA / Database Concepts

- **One-to-Many / Many-to-One relationship** — `Department` → `Employee` via `@OneToMany(mappedBy = ...)` and `@ManyToOne`
- **Cascade & Orphan Removal** — `CascadeType.ALL`, `orphanRemoval = true` for parent-managed child lifecycle
- **Fetch Strategies** — `FetchType.LAZY` to avoid unnecessary joins
- **Identity Generation Strategy** — `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- **Unique & Not-Null Constraints** — enforced at the column level (`@Column(unique = true, nullable = false)`)
- **Transaction Management** — `@Transactional` and `@Transactional(readOnly = true)` for read/write boundaries
- **Schema Auto-Generation** — `spring.jpa.hibernate.ddl-auto: update`
- **Connection Pooling** — HikariCP tuning via `application.yml`

## REST API Design

- **Resource-oriented URIs** — `/api/v1/departments`, `/api/v1/employees`
- **Proper HTTP verbs & status codes** — `201 Created`, `200 OK`, `404 Not Found`, `409 Conflict`, `400 Bad Request`
- **Query parameters for filtering** — `GET /api/v1/employees?departmentId={id}`
- **Versioned API base path** — `/api/v1`

## Validation & Error Handling

- Field-level validation annotations: `@NotBlank`, `@NotNull`, `@Email`, `@Size`, `@Pattern`, `@PastOrPresent`, `@DecimalMin`
- Custom business exceptions: `ResourceNotFoundException`, `DuplicateResourceException`
- Structured validation error responses with field-to-message maps

## Configuration

- **YAML-based configuration** (`application.yml`) for server, datasource, JPA, and logging settings
- **Environment variable overrides** for sensitive configuration (`${DB_USERNAME:root}`, `${DB_PASSWORD:root}`)

## Best Practices Applied

- Constructor injection via `@RequiredArgsConstructor` (no field injection)
- Immutable, single-purpose DTOs per use case (separate request/response DTOs)
- No entity leakage outside the service layer
- Defensive lookups (`findById().orElseThrow(...)`) to avoid `null` propagation
- Consistent, descriptive exception messages surfaced to API consumers
