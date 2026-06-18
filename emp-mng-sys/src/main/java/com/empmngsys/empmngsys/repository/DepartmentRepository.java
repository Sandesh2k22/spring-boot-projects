package com.empmngsys.empmngsys.repository;

import com.empmngsys.empmngsys.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
