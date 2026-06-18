package com.empmngsys.empmngsys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSummaryDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}
