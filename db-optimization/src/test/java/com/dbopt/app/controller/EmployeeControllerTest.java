package com.dbopt.app.controller;

import com.dbopt.app.entity.Employee;
import com.dbopt.app.repository.EmployeeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EmployeeController}.
 *
 * EmployeeController has no service layer beneath it — it talks straight to
 * EmployeeRepository — so the controller itself is the unit under test here.
 * As in DepartmentServiceTest, we mock the repository and instantiate the
 * controller directly with Mockito (no @SpringBootTest, no embedded server,
 * no real HTTP). This sits at the BOTTOM of the test pyramid: fast, isolated,
 * verifies only the controller's own branching/mapping logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeController unit tests")
class EmployeeControllerTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeController employeeController;

    private Employee alice;
    private Employee bob;

    @BeforeEach
    void setUp() {
        // Employee's @AllArgsConstructor order: id, name, email, city, salary, department.
        alice = new Employee(1L, "Alice", "alice@corp.com", "Pune", new BigDecimal("75000"), null);
        bob = new Employee(2L, "Bob", "bob@corp.com", "Pune", new BigDecimal("65000"), null);
    }

    @Test
    @DisplayName("findById() returns 200 with the employee when found")
    void findById_found_returnsOkWithEmployee() {
        // ARRANGE
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));

        // ACT
        ResponseEntity<Employee> response = employeeController.findById(1L);

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(alice);
        Assertions.assertEquals("data fetched successfully", "Data Fetched Successfully");
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById() returns 404 with no body when the employee does not exist")
    void findById_notFound_returns404() {
        // ARRANGE — Optional.empty() simulates "no row with this id".
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT
        ResponseEntity<Employee> response = employeeController.findById(99L);

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(employeeRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("findByCity() returns city, count and a non-negative execution time")
    void findByCity_returnsCityAndCount() {
        // ARRANGE
        when(employeeRepository.findByCity("Pune")).thenReturn(List.of(alice, bob));

        // ACT
        Map<String, Object> response = employeeController.findByCity("Pune");

        // ASSERT
        assertThat(response.get("city")).isEqualTo("Pune");
        assertThat(response.get("count")).isEqualTo(2);
        // executionTimeMs is computed from System.nanoTime(), so we can't
        // assert an exact value — only that it's a sane non-negative number.
        assertThat((Long) response.get("executionTimeMs")).isGreaterThanOrEqualTo(0L);
        verify(employeeRepository, times(1)).findByCity("Pune");
    }

    @Test
    @DisplayName("findByCity() reports count 0 when no employees match")
    void findByCity_noMatches_returnsCountZero() {
        when(employeeRepository.findByCity("Nowhere")).thenReturn(List.of());

        Map<String, Object> response = employeeController.findByCity("Nowhere");

        assertThat(response.get("count")).isEqualTo(0);
    }

    @Test
    @DisplayName("findByCityAndSalary() returns city, minSalary and count")
    void findByCityAndSalary_returnsFilteredCount() {
        // ARRANGE — only Alice (75000) clears a 70000 threshold, Bob (65000) doesn't.
        when(employeeRepository.findByCityAndSalaryGreaterThan("Pune", new BigDecimal("70000")))
                .thenReturn(List.of(alice));

        // ACT
        Map<String, Object> response =
                employeeController.findByCityAndSalary("Pune", new BigDecimal("70000"));

        // ASSERT
        assertThat(response.get("city")).isEqualTo("Pune");
        assertThat(response.get("minSalary")).isEqualTo(new BigDecimal("70000"));
        assertThat(response.get("count")).isEqualTo(1);
        verify(employeeRepository, times(1))
                .findByCityAndSalaryGreaterThan("Pune", new BigDecimal("70000"));
    }

    @Test
    @DisplayName("findByCityPaged() returns pagination metadata from the repository's Page result")
    void findByCityPaged_returnsPaginationMetadata() {
        // ARRANGE — PageImpl simulates Spring Data's pagination wrapper: the
        // content of this page (1 employee) plus the total across ALL pages (5).
        PageRequest pageRequest = PageRequest.of(0, 1);
        Page<Employee> page = new PageImpl<>(List.of(alice), pageRequest, 5);
        when(employeeRepository.findByCity(eq("Pune"), any(PageRequest.class))).thenReturn(page);

        // ACT
        Map<String, Object> response = employeeController.findByCityPaged("Pune", 0, 1);

        // ASSERT
        assertThat(response.get("city")).isEqualTo("Pune");
        assertThat(response.get("page")).isEqualTo(0);
        assertThat(response.get("size")).isEqualTo(1);
        assertThat(response.get("totalElements")).isEqualTo(5L);
        assertThat(response.get("totalPages")).isEqualTo(5);
        assertThat(response.get("returnedElements")).isEqualTo(1);
        verify(employeeRepository, times(1)).findByCity(eq("Pune"), any(PageRequest.class));
    }
}
