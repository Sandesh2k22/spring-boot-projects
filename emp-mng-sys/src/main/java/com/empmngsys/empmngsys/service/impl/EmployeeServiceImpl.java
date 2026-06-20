package com.empmngsys.empmngsys.service.impl;

import com.empmngsys.empmngsys.dto.EmployeeRequestDto;
import com.empmngsys.empmngsys.dto.EmployeeResponseDto;
import com.empmngsys.empmngsys.entity.Department;
import com.empmngsys.empmngsys.entity.Employee;
import com.empmngsys.empmngsys.exception.DuplicateResourceException;
import com.empmngsys.empmngsys.exception.ResourceNotFoundException;
import com.empmngsys.empmngsys.mapper.EmployeeMapper;
import com.empmngsys.empmngsys.repository.DepartmentRepository;
import com.empmngsys.empmngsys.repository.EmployeeRepository;
import com.empmngsys.empmngsys.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import java.util.List; // no longer needed: list-returning methods below are now paginated

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public EmployeeResponseDto createEmployee(EmployeeRequestDto requestDto) {
        if (employeeRepository.existsByEmailIgnoreCase(requestDto.getEmail())) {
            throw new DuplicateResourceException(
                    "Employee with email '" + requestDto.getEmail() + "' already exists");
        }
        Department department = findDepartmentOrThrow(requestDto.getDepartmentId());
        Employee employee = EmployeeMapper.toEntity(requestDto, department);
        Employee saved = employeeRepository.save(employee);
        return EmployeeMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(Long id) {
        Employee employee = findEmployeeOrThrow(id);
        return EmployeeMapper.toResponseDto(employee);
    }

    // @Override
    // @Transactional(readOnly = true)
    // public List<EmployeeResponseDto> getAllEmployees() {
    //     return employeeRepository.findAll().stream()
    //             .map(EmployeeMapper::toResponseDto)
    //             .toList();
    // }
    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDto> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(EmployeeMapper::toResponseDto);
    }

    // @Override
    // @Transactional(readOnly = true)
    // public List<EmployeeResponseDto> getEmployeesByDepartment(Long departmentId) {
    //     findDepartmentOrThrow(departmentId);
    //     return employeeRepository.findByDepartmentId(departmentId).stream()
    //             .map(EmployeeMapper::toResponseDto)
    //             .toList();
    // }
    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDto> getEmployeesByDepartment(Long departmentId, Pageable pageable) {
        findDepartmentOrThrow(departmentId);
        return employeeRepository.findByDepartmentId(departmentId, pageable)
                .map(EmployeeMapper::toResponseDto);
    }

    @Override
    public EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto requestDto) {
        Employee employee = findEmployeeOrThrow(id);

        boolean emailChanged = !employee.getEmail().equalsIgnoreCase(requestDto.getEmail());
        if (emailChanged && employeeRepository.existsByEmailIgnoreCase(requestDto.getEmail())) {
            throw new DuplicateResourceException(
                    "Employee with email '" + requestDto.getEmail() + "' already exists");
        }

        Department department = findDepartmentOrThrow(requestDto.getDepartmentId());
        EmployeeMapper.updateEntity(employee, requestDto, department);
        Employee updated = employeeRepository.save(employee);
        return EmployeeMapper.toResponseDto(updated);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeOrThrow(id);
        employeeRepository.delete(employee);
    }

    private Employee findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    private Department findDepartmentOrThrow(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + departmentId));
    }
}
