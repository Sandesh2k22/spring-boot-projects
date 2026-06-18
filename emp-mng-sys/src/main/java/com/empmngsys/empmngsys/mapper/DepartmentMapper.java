package com.empmngsys.empmngsys.mapper;

import com.empmngsys.empmngsys.dto.DepartmentRequestDto;
import com.empmngsys.empmngsys.dto.DepartmentResponseDto;
import com.empmngsys.empmngsys.dto.EmployeeSummaryDto;
import com.empmngsys.empmngsys.entity.Department;
import com.empmngsys.empmngsys.entity.Employee;

import java.util.Collections;
import java.util.List;

public final class DepartmentMapper {

    private DepartmentMapper() {
    }

    public static Department toEntity(DepartmentRequestDto dto) {
        Department department = new Department();
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        return department;
    }

    public static void updateEntity(Department department, DepartmentRequestDto dto) {
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
    }

    public static DepartmentResponseDto toResponseDto(Department department) {
        List<EmployeeSummaryDto> employees = department.getEmployees() == null
                ? Collections.emptyList()
                : department.getEmployees().stream()
                        .map(DepartmentMapper::toEmployeeSummaryDto)
                        .toList();

        return new DepartmentResponseDto(
                department.getId(),
                department.getName(),
                department.getDescription(),
                employees);
    }

    private static EmployeeSummaryDto toEmployeeSummaryDto(Employee employee) {
        return new EmployeeSummaryDto(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail());
    }
}
