package com.empmngsys.empmngsys.repository;

import com.empmngsys.empmngsys.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByDepartmentId(Long departmentId);

    Optional<Employee> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
