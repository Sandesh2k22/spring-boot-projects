package com.dbopt.app.controller;

import com.dbopt.app.entity.Employee;
import com.dbopt.app.repository.EmployeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
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
        summary = "Filter by city — toggle index on/off to compare execution time",
        description = "indexed=true  → normal query, MySQL uses idx_employee_city (index scan).\n" +
                      "indexed=false → IGNORE INDEX hint forces a full table scan, simulating no-index performance.\n" +
                      "Run both and compare executionTimeMs to see the real impact of indexing."
    )
    @GetMapping("/by-city")
    public Map<String, Object> findByCity(
            @Parameter(description = "City name to filter on", example = "Pune") @RequestParam String city,
            @Parameter(description = "true = use index (fast), false = ignore index (full table scan)")
            @RequestParam(defaultValue = "true") boolean indexed) {

        long start = System.nanoTime();
        List<Employee> result = indexed
                ? employeeRepository.findByCity(city)
                : employeeRepository.findByCityIgnoreIndex(city);
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        Map<String, Object> response = new HashMap<>();
        response.put("city", city);
        response.put("indexed", indexed);
        response.put("scanType", indexed ? "INDEX SCAN (idx_employee_city)" : "FULL TABLE SCAN (IGNORE INDEX)");
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
