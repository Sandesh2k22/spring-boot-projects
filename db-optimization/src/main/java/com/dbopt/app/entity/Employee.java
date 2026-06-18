package com.dbopt.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employee_city", columnList = "city"),
        @Index(name = "idx_employee_city_salary", columnList = "city, salary")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(nullable = false)
    private BigDecimal salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}
