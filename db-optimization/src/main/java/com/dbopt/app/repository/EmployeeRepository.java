package com.dbopt.app.repository;

import com.dbopt.app.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Step 2: query filtering on a non-indexed column
    List<Employee> findByCity(String city);

    // Step 6: query filtering on the composite index columns (city + salary)
    List<Employee> findByCityAndSalaryGreaterThan(String city, BigDecimal salary);

    // Step 8: pagination
    Page<Employee> findByCity(String city, Pageable pageable);

    // Forces a full table scan regardless of existing indexes — used to demonstrate
    // without-index performance from Swagger without dropping the real index.
    @Query(value = "SELECT * FROM employees IGNORE INDEX (idx_employee_city, idx_employee_city_salary) WHERE city = :city",
           nativeQuery = true)
    List<Employee> findByCityIgnoreIndex(@Param("city") String city);
}
