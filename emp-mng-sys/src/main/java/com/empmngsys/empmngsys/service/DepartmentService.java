package com.empmngsys.empmngsys.service;

import com.empmngsys.empmngsys.dto.DepartmentRequestDto;
import com.empmngsys.empmngsys.dto.DepartmentResponseDto;

import java.util.List;

public interface DepartmentService {

    DepartmentResponseDto createDepartment(DepartmentRequestDto requestDto);

    DepartmentResponseDto getDepartmentById(Long id);

    List<DepartmentResponseDto> getAllDepartments();

    DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto requestDto);

    void deleteDepartment(Long id);
}
