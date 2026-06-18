package com.dbopt.app.config;

import com.dbopt.app.entity.Department;
import com.dbopt.app.entity.Employee;
import com.dbopt.app.repository.DepartmentRepository;
import com.dbopt.app.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final int TOTAL_EMPLOYEES = 10_000;
    private static final int BATCH_SIZE = 500;

    private static final String[] DEPARTMENTS = {"Engineering", "Sales", "HR", "Marketing", "Finance"};

    // 'Pune' is deliberately overrepresented so a city filter returns a
    // realistic, sizable result set for the slow-query demo.
    private static final String[] CITIES = {
            "Pune", "Pune", "Pune", "Mumbai", "Delhi", "Bengaluru", "Chennai", "Hyderabad", "Nagpur", "Kolkata"
    };

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            System.out.println("Employees table already populated (" + employeeRepository.count() + " rows). Skipping seed.");
            return;
        }

        List<Department> departments = new ArrayList<>();
        for (String name : DEPARTMENTS) {
            Department d = new Department();
            d.setName(name);
            departments.add(d);
        }
        departments = departmentRepository.saveAll(departments);

        Random random = new Random();
        List<Employee> batch = new ArrayList<>(BATCH_SIZE);

        for (int i = 1; i <= TOTAL_EMPLOYEES; i++) {
            Employee e = new Employee();
            e.setName("Employee " + i);
            e.setEmail("employee" + i + "@example.com");
            e.setCity(CITIES[random.nextInt(CITIES.length)]);
            e.setSalary(BigDecimal.valueOf(30000 + random.nextInt(70000)));
            e.setDepartment(departments.get(random.nextInt(departments.size())));
            batch.add(e);

            if (batch.size() == BATCH_SIZE) {
                employeeRepository.saveAll(batch);
                batch.clear();
                System.out.println("Inserted " + i + " employees...");
            }
        }
        if (!batch.isEmpty()) {
            employeeRepository.saveAll(batch);
        }

        System.out.println("Seeding complete. Total employees: " + employeeRepository.count());
    }
}
