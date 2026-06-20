package com.empmngsys.empmngsys.controller;

import com.empmngsys.empmngsys.dto.ApiResponse;
import com.empmngsys.empmngsys.dto.EmployeeRequestDto;
import com.empmngsys.empmngsys.dto.EmployeeResponseDto;
import com.empmngsys.empmngsys.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// import java.util.List; // no longer needed: getAllEmployees now returns a paginated Page

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> createEmployee(
            @Valid @RequestBody EmployeeRequestDto requestDto) {
        EmployeeResponseDto created = employeeService.createEmployee(requestDto);
        return new ResponseEntity<>(
                ApiResponse.success("Employee created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> getEmployeeById(@PathVariable Long id) {
        EmployeeResponseDto employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", employee));
    }

    // @GetMapping
    // public ResponseEntity<ApiResponse<List<EmployeeResponseDto>>> getAllEmployees(
    //         @RequestParam(required = false) Long departmentId) {
    //     List<EmployeeResponseDto> employees = departmentId != null
    //             ? employeeService.getEmployeesByDepartment(departmentId)
    //             : employeeService.getAllEmployees();
    //     return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    // }
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EmployeeResponseDto>>> getAllEmployees(
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<EmployeeResponseDto> employees = departmentId != null
                ? employeeService.getEmployeesByDepartment(departmentId, pageable)
                : employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> updateEmployee(
            @PathVariable Long id, @Valid @RequestBody EmployeeRequestDto requestDto) {
        EmployeeResponseDto updated = employeeService.updateEmployee(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully"));
    }
}
