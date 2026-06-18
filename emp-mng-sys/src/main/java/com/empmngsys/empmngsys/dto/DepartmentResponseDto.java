package com.empmngsys.empmngsys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponseDto {

    private Long id;
    private String name;
    private String description;
    private List<EmployeeSummaryDto> employees;
}
