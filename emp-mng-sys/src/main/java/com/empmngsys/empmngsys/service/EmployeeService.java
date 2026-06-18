package com.empmngsys.empmngsys.service;

import com.empmngsys.empmngsys.dto.EmployeeRequestDto;
import com.empmngsys.empmngsys.dto.EmployeeResponseDto;

import java.util.List;

public interface EmployeeService {

    EmployeeResponseDto createEmployee(EmployeeRequestDto requestDto);

    EmployeeResponseDto getEmployeeById(Long id);

    List<EmployeeResponseDto> getAllEmployees();

    List<EmployeeResponseDto> getEmployeesByDepartment(Long departmentId);

    EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto requestDto);

    void deleteEmployee(Long id);
}
