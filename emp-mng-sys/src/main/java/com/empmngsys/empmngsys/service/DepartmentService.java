package com.empmngsys.empmngsys.service;

import com.empmngsys.empmngsys.dto.DepartmentRequestDto;
import com.empmngsys.empmngsys.dto.DepartmentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// import java.util.List; // no longer needed: getAllDepartments is now paginated

public interface DepartmentService {

    DepartmentResponseDto createDepartment(DepartmentRequestDto requestDto);

    DepartmentResponseDto getDepartmentById(Long id);

    // List<DepartmentResponseDto> getAllDepartments(); // replaced: now takes Pageable and returns a Page
    Page<DepartmentResponseDto> getAllDepartments(Pageable pageable);

    DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto requestDto);

    void deleteDepartment(Long id);
}
