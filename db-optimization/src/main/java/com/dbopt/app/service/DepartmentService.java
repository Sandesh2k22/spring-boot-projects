package com.dbopt.app.service;

import com.dbopt.app.entity.Department;
import com.dbopt.app.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    // Triggers N+1: 1 query for departments, then 1 additional query per
    // department when employees.size() is accessed lazily.
    @Transactional(readOnly = true)
    public Map<String, Integer> getDepartmentEmployeeCountsNPlusOne() {
        List<Department> departments = departmentRepository.findAllPlain();
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Department d : departments) {
            result.put(d.getName(), d.getEmployees().size());
        }
        return result;
    }

    // Fixed: single query with JOIN FETCH loads departments and employees together.
    @Transactional(readOnly = true)
    public Map<String, Integer> getDepartmentEmployeeCountsFetchJoin() {
        List<Department> departments = departmentRepository.findAllWithEmployees();
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Department d : departments) {
            result.put(d.getName(), d.getEmployees().size());
        }
        return result;
    }
}
