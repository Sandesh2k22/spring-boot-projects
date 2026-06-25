package com.empmngsys.empmngsys.service;

import com.empmngsys.empmngsys.dto.EmployeeRequestDto;
import com.empmngsys.empmngsys.dto.EmployeeResponseDto;
import com.empmngsys.empmngsys.entity.Department;
import com.empmngsys.empmngsys.entity.Employee;
import com.empmngsys.empmngsys.exception.DuplicateResourceException;
import com.empmngsys.empmngsys.exception.ResourceNotFoundException;
import com.empmngsys.empmngsys.repository.DepartmentRepository;
import com.empmngsys.empmngsys.repository.EmployeeRepository;
import com.empmngsys.empmngsys.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Department department;
    private EmployeeRequestDto requestDto;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("Engineering");

        requestDto = new EmployeeRequestDto();
        requestDto.setFirstName("Rohit");
        requestDto.setLastName("Sharma");
        requestDto.setEmail("rohit.sharma@dev.com");
        requestDto.setPhoneNumber("+919876543210");
        requestDto.setDateOfJoining(LocalDate.of(2024, 1, 15));
        requestDto.setSalary(new BigDecimal("75000.00"));
        requestDto.setDepartmentId(1L);
    }

    private Employee persistedEmployee() {
        Employee employee = new Employee();
        employee.setId(10L);
        employee.setFirstName("Rohit");
        employee.setLastName("Sharma");
        employee.setEmail("rohit.sharma@dev.com");
        employee.setPhoneNumber("+919876543210");
        employee.setDateOfJoining(LocalDate.of(2024, 1, 15));
        employee.setSalary(new BigDecimal("75000.00"));
        employee.setDepartment(department);
        return employee;
    }

    @Test
    void createEmployee_persistsAndReturnsDto() {
        when(employeeRepository.existsByEmailIgnoreCase(requestDto.getEmail())).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(persistedEmployee());

        EmployeeResponseDto result = employeeService.createEmployee(requestDto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getEmail()).isEqualTo("rohit.sharma@dev.com");
        assertThat(result.getDepartmentId()).isEqualTo(1L);
        assertThat(result.getDepartmentName()).isEqualTo("Engineering");

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertThat(captor.getValue().getDepartment()).isEqualTo(department);
    }

    @Test
    void createEmployee_duplicateEmail_throwsAndDoesNotSave() {
        when(employeeRepository.existsByEmailIgnoreCase(requestDto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(requestDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("rohit.sharma@dev.com");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_missingDepartment_throwsResourceNotFound() {
        when(employeeRepository.existsByEmailIgnoreCase(requestDto.getEmail())).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createEmployee(requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void getEmployeeById_found_returnsDto() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(persistedEmployee()));

        EmployeeResponseDto result = employeeService.getEmployeeById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getFirstName()).isEqualTo("Rohit");
    }

    @Test
    void getEmployeeById_missing_throwsResourceNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 99");
    }

    @Test
    void getAllEmployees_mapsPageContents() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(List.of(persistedEmployee()), pageable, 1);
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        Page<EmployeeResponseDto> result = employeeService.getAllEmployees(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("rohit.sharma@dev.com");
    }

    @Test
    void getEmployeesByDepartment_unknownDepartment_throws() {
        Pageable pageable = PageRequest.of(0, 10);
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeesByDepartment(1L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(employeeRepository, never()).findByDepartmentId(any(), any());
    }

    @Test
    void updateEmployee_sameEmail_skipsDuplicateCheck() {
        Employee existing = persistedEmployee();
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existing);

        requestDto.setLastName("Verma");
        EmployeeResponseDto result = employeeService.updateEmployee(10L, requestDto);

        assertThat(result.getLastName()).isEqualTo("Verma");
        verify(employeeRepository, never()).existsByEmailIgnoreCase(any());
    }

    @Test
    void updateEmployee_newEmailAlreadyTaken_throws() {
        Employee existing = persistedEmployee();
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(employeeRepository.existsByEmailIgnoreCase("new@dev.com")).thenReturn(true);

        requestDto.setEmail("new@dev.com");

        assertThatThrownBy(() -> employeeService.updateEmployee(10L, requestDto))
                .isInstanceOf(DuplicateResourceException.class);

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void deleteEmployee_missing_throwsAndDoesNotDelete() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(employeeRepository, never()).delete(any());
    }

    @Test
    void deleteEmployee_found_deletes() {
        Employee existing = persistedEmployee();
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(existing));

        employeeService.deleteEmployee(10L);

        verify(employeeRepository).delete(existing);
    }
}
