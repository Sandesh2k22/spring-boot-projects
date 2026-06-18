package com.empmngsys.empmngsys.mapper;

import com.empmngsys.empmngsys.dto.EmployeeRequestDto;
import com.empmngsys.empmngsys.dto.EmployeeResponseDto;
import com.empmngsys.empmngsys.entity.Department;
import com.empmngsys.empmngsys.entity.Employee;

public final class EmployeeMapper {

    private EmployeeMapper() {
    }

    public static Employee toEntity(EmployeeRequestDto dto, Department department) {
        Employee employee = new Employee();
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setDateOfJoining(dto.getDateOfJoining());
        employee.setSalary(dto.getSalary());
        employee.setDepartment(department);
        return employee;
    }

    public static void updateEntity(Employee employee, EmployeeRequestDto dto, Department department) {
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setDateOfJoining(dto.getDateOfJoining());
        employee.setSalary(dto.getSalary());
        employee.setDepartment(department);
    }

    public static EmployeeResponseDto toResponseDto(Employee employee) {
        return new EmployeeResponseDto(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhoneNumber(),
                employee.getDateOfJoining(),
                employee.getSalary(),
                employee.getDepartment().getId(),
                employee.getDepartment().getName());
    }
}
