package com.empmngsys.empmngsys.service;

import com.empmngsys.empmngsys.dto.EmployeeRequestDto;
import com.empmngsys.empmngsys.dto.EmployeeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// import java.util.List; // no longer needed: list-returning methods below are now paginated

public interface EmployeeService {

    EmployeeResponseDto createEmployee(EmployeeRequestDto requestDto);

    EmployeeResponseDto getEmployeeById(Long id);

    // List<EmployeeResponseDto> getAllEmployees(); // replaced: now takes Pageable and returns a Page
    Page<EmployeeResponseDto> getAllEmployees(Pageable pageable);

    // List<EmployeeResponseDto> getEmployeesByDepartment(Long departmentId); // replaced: now paginated
    Page<EmployeeResponseDto> getEmployeesByDepartment(Long departmentId, Pageable pageable);

    EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto requestDto);

    void deleteEmployee(Long id);
}
