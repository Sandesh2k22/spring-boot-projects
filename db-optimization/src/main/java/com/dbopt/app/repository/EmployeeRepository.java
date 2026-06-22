package com.dbopt.app.repository;

import com.dbopt.app.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Filters on idx_employee_city
    List<Employee> findByCity(String city);

    // Filters on idx_employee_city_salary (city, salary)
    List<Employee> findByCityAndSalaryGreaterThan(String city, BigDecimal salary);

    // Pagination over idx_employee_city
    Page<Employee> findByCity(String city, Pageable pageable);
}
