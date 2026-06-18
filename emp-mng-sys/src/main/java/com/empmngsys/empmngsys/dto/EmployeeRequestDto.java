package com.empmngsys.empmngsys.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeRequestDto {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number must be 7-15 digits, optionally prefixed with +")
    private String phoneNumber;

    @NotNull(message = "Date of joining is required")
    @PastOrPresent(message = "Date of joining cannot be in the future")
    private LocalDate dateOfJoining;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
    private BigDecimal salary;

    @NotNull(message = "Department id is required")
    private Long departmentId;
}
