# DB Optimization

A hands-on Spring Boot project demonstrating database query performance analysis,
indexing strategies, and fixing the JPA N+1 problem — using a MySQL database
seeded with 10,000 employee records.

## Setup

```sql
CREATE DATABASE db_optimization_db;
```

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/db_optimization_db
spring.datasource.username=springuser
spring.datasource.password=123
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

On first run, [`DataSeeder`](src/main/java/com/dbopt/app/config/DataSeeder.java) batch-inserts:
- 5 departments (Engineering, Sales, HR, Marketing, Finance)
- 10,000 employees, distributed across 8 cities (Pune is overrepresented at ~2,942 rows)

## Domain Model

- `Department` 1 — N `Employee` (lazy `@OneToMany`)
- `Employee.city` is the column used to demonstrate indexing — initially **no index**

---

## 1. Sample Dataset

10,000 employee rows inserted in batches of 500 via Hibernate batch inserts
(`hibernate.jdbc.batch_size=500`, `order_inserts=true`).

```
SELECT COUNT(*) FROM employees;  -- 10000
```

## 2. Slow Query (Non-Indexed Column)

`GET /api/employees/by-city?city=Pune` runs:

```sql
SELECT * FROM employees WHERE city = 'Pune';
```

| Run | Rows | App execution time |
|---|---|---|
| 1 (cold) | 2942 | 124 ms |
| 2 | 2942 | 39 ms |
| 3 | 2942 | 24 ms |

Raw SQL profiled with MySQL `SHOW PROFILES`: **3.05 ms**.

## 3. Execution Plan (Before Index)

```sql
EXPLAIN SELECT * FROM employees WHERE city='Pune';
```

| type | possible_keys | key | rows | filtered | Extra |
|---|---|---|---|---|---|
| **ALL** | NULL | NULL | 10081 | 10.00% | Using where |

`query_cost = 1032.35` (EXPLAIN FORMAT=JSON). MySQL performs a **full table scan**,
reading all 10,081 rows and discarding ~90% after the fact — `city` has no index
so neither `possible_keys` nor `key` reference one.

## 4. Add Single-Column Index

Declared in [`Employee`](src/main/java/com/dbopt/app/entity/Employee.java):

```java
@Table(name = "employees", indexes = {
    @Index(name = "idx_employee_city", columnList = "city")
})
```

Hibernate (`ddl-auto=update`) ran `CREATE INDEX idx_employee_city ON employees (city)`.
Verified via `SHOW INDEX FROM employees`.

## 5. Performance Comparison (After Index)

```sql
EXPLAIN SELECT * FROM employees WHERE city='Pune';
```

| Metric | Before | After |
|---|---|---|
| `type` | ALL (full scan) | **ref** (index lookup) |
| `key` | NULL | idx_employee_city |
| `rows` examined | 10081 | 2942 |
| `filtered` | 10.00% | 100.00% |
| `query_cost` | 1032.35 | **366.95** (~64% lower) |
| Raw SQL time (`SHOW PROFILES`) | 3.05 ms | **0.77 ms** (~4x faster) |

App-level endpoint stayed ~24-26ms at 10K rows (entity hydration of 2,942 rows
dominates), but the **database-side cost and scan size dropped significantly** —
this gap widens dramatically as the table grows (100K, 1M+ rows).

## 6. Composite Index

Added `(city, salary)`:

```java
@Index(name = "idx_employee_city_salary", columnList = "city, salary")
```

Query: `SELECT * FROM employees WHERE city='Pune' AND salary > 60000` (1,702 of 2,942 match).

| Index used | type | rows examined | Extra | Time |
|---|---|---|---|---|
| `idx_employee_city` only | ref | 2942 | Using where (salary filtered post-fetch) | 3.40 ms |
| `idx_employee_city_salary` | range | ~1 | **Using index condition** (ICP) | **0.82 ms** |

The composite index lets MySQL apply the `salary` filter *inside* the index via
Index Condition Pushdown, scanning only the matching range instead of fetching
every "Pune" row and filtering afterward.

> Note: the optimizer picked `idx_employee_city` by default for this query
> (equal estimated cost) — `FORCE INDEX` was used to show the composite index's
> actual benefit. **Index existence doesn't guarantee usage**; the optimizer
> decides based on cardinality estimates.

Endpoint: `GET /api/employees/by-city-and-salary?city=Pune&minSalary=60000`

## 7. N+1 Problem & Fix

`GET /api/departments/n-plus-one` ([DepartmentService](src/main/java/com/dbopt/app/service/DepartmentService.java)):

```java
List<Department> departments = departmentRepository.findAllPlain();
for (Department d : departments) {
    result.put(d.getName(), d.getEmployees().size()); // lazy load per department
}
```

**SQL log: 6 queries** for 5 departments — 1 for `departments`, plus 1
`SELECT ... FROM employees WHERE department_id=?` **per department**.

**Fix** — `GET /api/departments/fetch-join`:

```java
@Query("SELECT DISTINCT d FROM Department d JOIN FETCH d.employees")
List<Department> findAllWithEmployees();
```

**SQL log: 1 query** — a single `JOIN` loads departments and employees together.

## 8. Pagination

`GET /api/employees/by-city/paged?city=Pune&page=0&size=20`

```java
Page<Employee> findByCity(String city, Pageable pageable);
```

Generates `LIMIT`/`OFFSET` SQL plus a `COUNT(*)` for `totalElements`/`totalPages`,
instead of loading all 2,942 matching rows:

```sql
select e1_0.id, ... from employees e1_0 where e1_0.city=? limit ?
select count(e1_0.id) from employees e1_0 where e1_0.city=?
```

| Request | Result |
|---|---|
| page=0, size=20 | 20 rows, totalPages=148 |
| page=1, size=20 | 20 rows |
| page=5, size=500 | 442 rows (last partial page), totalPages=6 |

---

## Learning Summary

- **Indexing**: a missing index forces MySQL into a full table scan (`type=ALL`),
  reading every row even when only a fraction match. A single-column index turns
  this into a direct lookup (`type=ref`), cutting query cost by ~64% and execution
  time by ~4x in this dataset — and the saving grows with table size.
- **`EXPLAIN`** is the primary tool for understanding *how* MySQL executes a query:
  `type`, `key`, `rows`, and `filtered` reveal whether an index is used and how
  selective it is.
- **Composite indexes** help multi-column filters via Index Condition Pushdown,
  but the optimizer may not always choose the "best" index — verify with `EXPLAIN`
  and `FORCE INDEX` when investigating.
- **N+1 queries** are a common JPA pitfall with lazy `@OneToMany`/`@ManyToOne`
  associations — each lazy access in a loop triggers a separate query. `JOIN FETCH`
  collapses N+1 queries into 1.
- **Pagination** (`Pageable`/`Page<T>`) avoids loading entire result sets into
  memory — essential for any endpoint that can return large collections.
