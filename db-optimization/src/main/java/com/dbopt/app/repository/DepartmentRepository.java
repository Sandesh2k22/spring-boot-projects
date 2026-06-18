package com.dbopt.app.repository;

import com.dbopt.app.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Step 7: triggers N+1 queries when employees are accessed lazily
    @Query("SELECT d FROM Department d")
    List<Department> findAllPlain();

    // Step 7 fix: fetch join loads employees in a single query
    @Query("SELECT DISTINCT d FROM Department d JOIN FETCH d.employees")
    List<Department> findAllWithEmployees();
}
