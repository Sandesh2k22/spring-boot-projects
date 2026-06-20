package com.empmngsys.empmngsys.service.impl;

import com.empmngsys.empmngsys.dto.DepartmentRequestDto;
import com.empmngsys.empmngsys.dto.DepartmentResponseDto;
import com.empmngsys.empmngsys.entity.Department;
import com.empmngsys.empmngsys.exception.DuplicateResourceException;
import com.empmngsys.empmngsys.exception.ResourceNotFoundException;
import com.empmngsys.empmngsys.mapper.DepartmentMapper;
import com.empmngsys.empmngsys.repository.DepartmentRepository;
import com.empmngsys.empmngsys.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import java.util.List; // no longer needed: getAllDepartments is now paginated

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public DepartmentResponseDto createDepartment(DepartmentRequestDto requestDto) {
        if (departmentRepository.existsByNameIgnoreCase(requestDto.getName())) {
            throw new DuplicateResourceException(
                    "Department with name '" + requestDto.getName() + "' already exists");
        }
        Department department = DepartmentMapper.toEntity(requestDto);
        Department saved = departmentRepository.save(department);
        return DepartmentMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponseDto getDepartmentById(Long id) {
        Department department = findDepartmentOrThrow(id);
        return DepartmentMapper.toResponseDto(department);
    }

    // @Override
    // @Transactional(readOnly = true)
    // public List<DepartmentResponseDto> getAllDepartments() {
    //     return departmentRepository.findAll().stream()
    //             .map(DepartmentMapper::toResponseDto)
    //             .toList();
    // }
    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentResponseDto> getAllDepartments(Pageable pageable) {
        return departmentRepository.findAll(pageable)
                .map(DepartmentMapper::toResponseDto);
    }

    @Override
    public DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto requestDto) {
        Department department = findDepartmentOrThrow(id);

        boolean nameChanged = !department.getName().equalsIgnoreCase(requestDto.getName());
        if (nameChanged && departmentRepository.existsByNameIgnoreCase(requestDto.getName())) {
            throw new DuplicateResourceException(
                    "Department with name '" + requestDto.getName() + "' already exists");
        }

        DepartmentMapper.updateEntity(department, requestDto);
        Department updated = departmentRepository.save(department);
        return DepartmentMapper.toResponseDto(updated);
    }

    @Override
    public void deleteDepartment(Long id) {
        Department department = findDepartmentOrThrow(id);
        departmentRepository.delete(department);
    }

    private Department findDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }
}
