package com.exam.employee;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import tools.jackson.databind.json.JsonMapper;

@Repository
public class EmployeeRepository {

    private final File file;
    private final JsonMapper mapper;

    public EmployeeRepository(@Value("${employees.file:data/employees.json}") File file, JsonMapper mapper) {
        this.file = file;
        this.mapper = mapper;
    }

    public synchronized List<Employee> findAll() {
        return file.exists() ? List.of(mapper.readValue(file, Employee[].class)) : List.of();
    }

    public Optional<Employee> findById(long id) {
        return findAll().stream().filter(e -> e.id() == id).findFirst();
    }

    public synchronized long save(Employee employee) {
        List<Employee> employees = new ArrayList<>(findAll());
        long id = employees.stream().mapToLong(e -> e.id()).max().orElse(0) + 1;
        employees.add(employee.withId(id));
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, employees);
        return id;
    }
}
