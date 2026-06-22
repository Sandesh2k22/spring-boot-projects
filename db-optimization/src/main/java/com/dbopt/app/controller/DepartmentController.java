package com.dbopt.app.controller;

import com.dbopt.app.entity.Department;
import com.dbopt.app.repository.DepartmentRepository;
import com.dbopt.app.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Department", description = "JPA N+1 problem demo and fix via JOIN FETCH")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentRepository departmentRepository;

    @Operation(
        summary = "Find department by ID (primary key lookup)",
        description = "Uses findById, a direct primary key lookup via the clustered index — fastest possible query."
    )
    @GetMapping("/{id}")
    public ResponseEntity<Department> findById(
            @Parameter(description = "Department ID", example = "1") @PathVariable Long id) {
        return departmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "N+1 problem (bad) — 1 query for departments + 1 per department for employees",
        description = "Fetches all departments lazily. Accessing employees.size() per department " +
                      "triggers a separate SELECT for each one. Watch Hibernate SQL logs to count queries."
    )
    @GetMapping("/n-plus-one")
    public Map<String, Integer> nPlusOne() {
        return departmentService.getDepartmentEmployeeCountsNPlusOne();
    }

    @Operation(
        summary = "N+1 fixed — single query using JOIN FETCH",
        description = "Uses 'SELECT DISTINCT d FROM Department d JOIN FETCH d.employees' to load " +
                      "departments and their employees in one SQL join. Same result, one query."
    )
    @GetMapping("/fetch-join")
    public Map<String, Integer> fetchJoin() {
        return departmentService.getDepartmentEmployeeCountsFetchJoin();
    }
}
