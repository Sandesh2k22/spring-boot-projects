package com.empmngsys.empmngsys.service;

import com.empmngsys.empmngsys.dto.DepartmentRequestDto;
import com.empmngsys.empmngsys.dto.DepartmentResponseDto;
import com.empmngsys.empmngsys.entity.Department;
import com.empmngsys.empmngsys.exception.DuplicateResourceException;
import com.empmngsys.empmngsys.exception.ResourceNotFoundException;
import com.empmngsys.empmngsys.repository.DepartmentRepository;
import com.empmngsys.empmngsys.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private DepartmentRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new DepartmentRequestDto();
        requestDto.setName("Engineering");
        requestDto.setDescription("Builds the product");
    }

    private Department persistedDepartment() {
        Department department = new Department();
        department.setId(1L);
        department.setName("Engineering");
        department.setDescription("Builds the product");
        return department;
    }

    @Test
    void createDepartment_persistsAndReturnsDto() {
        when(departmentRepository.existsByNameIgnoreCase("Engineering")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(persistedDepartment());

        DepartmentResponseDto result = departmentService.createDepartment(requestDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Engineering");
        assertThat(result.getEmployees()).isEmpty();
    }

    @Test
    void createDepartment_duplicateName_throwsAndDoesNotSave() {
        when(departmentRepository.existsByNameIgnoreCase("Engineering")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(requestDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Engineering");

        verify(departmentRepository, never()).save(any());
    }

    @Test
    void getDepartmentById_missing_throwsResourceNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found with id: 99");
    }

    @Test
    void getAllDepartments_mapsPageContents() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Department> page = new PageImpl<>(List.of(persistedDepartment()), pageable, 1);
        when(departmentRepository.findAll(pageable)).thenReturn(page);

        Page<DepartmentResponseDto> result = departmentService.getAllDepartments(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Engineering");
    }

    @Test
    void updateDepartment_renameToExistingName_throws() {
        Department existing = persistedDepartment();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.existsByNameIgnoreCase("Sales")).thenReturn(true);

        requestDto.setName("Sales");

        assertThatThrownBy(() -> departmentService.updateDepartment(1L, requestDto))
                .isInstanceOf(DuplicateResourceException.class);

        verify(departmentRepository, never()).save(any());
    }

    @Test
    void updateDepartment_sameName_updatesDescription() {
        Department existing = persistedDepartment();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.save(any(Department.class))).thenReturn(existing);

        requestDto.setDescription("Updated description");
        DepartmentResponseDto result = departmentService.updateDepartment(1L, requestDto);

        assertThat(result.getDescription()).isEqualTo("Updated description");
        verify(departmentRepository, never()).existsByNameIgnoreCase(any());
    }

    @Test
    void deleteDepartment_found_deletes() {
        Department existing = persistedDepartment();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));

        departmentService.deleteDepartment(1L);

        verify(departmentRepository).delete(existing);
    }
}
