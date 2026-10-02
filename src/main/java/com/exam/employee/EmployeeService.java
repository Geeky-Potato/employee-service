package com.exam.employee;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public long create(Employee employee) {
        return repository.save(employee);
    }

    public Optional<Employee> findById(long id) {
        return repository.findById(id);
    }

    public List<Employee> search(String name, BigDecimal fromSalary, BigDecimal toSalary) {
        String query = name == null ? null : name.toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(e -> query == null
                        || e.firstName().toLowerCase(Locale.ROOT).contains(query)
                        || e.lastName().toLowerCase(Locale.ROOT).contains(query))
                .filter(e -> fromSalary == null || e.salary().compareTo(fromSalary) >= 0)
                .filter(e -> toSalary == null || e.salary().compareTo(toSalary) <= 0)
                .toList();
    }
}
