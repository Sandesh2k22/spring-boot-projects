package com.empmngsys.empmngsys.controller;

import com.empmngsys.empmngsys.dto.ApiResponse;
import com.empmngsys.empmngsys.dto.DepartmentRequestDto;
import com.empmngsys.empmngsys.dto.DepartmentResponseDto;
import com.empmngsys.empmngsys.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> createDepartment(
            @Valid @RequestBody DepartmentRequestDto requestDto) {
        DepartmentResponseDto created = departmentService.createDepartment(requestDto);
        return new ResponseEntity<>(
                ApiResponse.success("Department created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> getDepartmentById(@PathVariable Long id) {
        DepartmentResponseDto department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Department retrieved successfully", department));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponseDto>>> getAllDepartments() {
        List<DepartmentResponseDto> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> updateDepartment(
            @PathVariable Long id, @Valid @RequestBody DepartmentRequestDto requestDto) {
        DepartmentResponseDto updated = departmentService.updateDepartment(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully"));
    }
}
