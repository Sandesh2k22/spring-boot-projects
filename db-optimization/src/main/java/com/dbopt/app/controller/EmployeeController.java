package com.dbopt.app.controller;

import com.dbopt.app.entity.Employee;
import com.dbopt.app.repository.EmployeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Query performance demos — indexing, composite index, pagination")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    @Operation(
        summary = "Find employee by ID (primary key lookup)",
        description = "Uses findById, a direct primary key lookup via the clustered index — fastest possible query."
    )
    @GetMapping("/{id}")
    public ResponseEntity<Employee> findById(
            @Parameter(description = "Employee ID", example = "1") @PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Filter by city",
        description = "Uses idx_employee_city (index scan) to filter employees by city."
    )
    @GetMapping("/by-city")
    public Map<String, Object> findByCity(
            @Parameter(description = "City name to filter on", example = "Pune") @RequestParam String city) {

        long start = System.nanoTime();
        List<Employee> result = employeeRepository.findByCity(city);
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        Map<String, Object> response = new HashMap<>();
        response.put("city", city);
        response.put("count", result.size());
        response.put("executionTimeMs", durationMs);
        return response;
    }

    @Operation(
        summary = "Filter by city + minimum salary (composite index demo)",
        description = "Uses idx_employee_city_salary (city, salary) to apply Index Condition Pushdown. " +
                      "Compare with FORCE INDEX(idx_employee_city) via EXPLAIN to see the difference."
    )
    @GetMapping("/by-city-and-salary")
    public Map<String, Object> findByCityAndSalary(
            @Parameter(description = "City name", example = "Pune") @RequestParam String city,
            @Parameter(description = "Minimum salary (exclusive)", example = "60000") @RequestParam BigDecimal minSalary) {
        long start = System.nanoTime();
        List<Employee> result = employeeRepository.findByCityAndSalaryGreaterThan(city, minSalary);
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        Map<String, Object> response = new HashMap<>();
        response.put("city", city);
        response.put("minSalary", minSalary);
        response.put("count", result.size());
        response.put("executionTimeMs", durationMs);
        return response;
    }

    @Operation(
        summary = "Paginated city filter (pagination demo)",
        description = "Returns one page of employees filtered by city using LIMIT/OFFSET. " +
                      "Avoids loading the entire result set into memory."
    )
    @GetMapping("/by-city/paged")
    public Map<String, Object> findByCityPaged(
            @Parameter(description = "City name", example = "Pune") @RequestParam String city,
            @Parameter(description = "Zero-based page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {
        long start = System.nanoTime();
        Page<Employee> result = employeeRepository.findByCity(city, PageRequest.of(page, size));
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        Map<String, Object> response = new HashMap<>();
        response.put("city", city);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("returnedElements", result.getNumberOfElements());
        response.put("executionTimeMs", durationMs);
        return response;
    }
}
