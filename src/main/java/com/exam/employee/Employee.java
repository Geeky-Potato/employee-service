package com.exam.employee;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PositiveOrZero;

public record Employee(
        Long id,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull @Past LocalDate dateOfBirth,
        @NotNull @PositiveOrZero @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal salary,
        @NotNull LocalDate joinDate,
        @NotBlank String department) {

    Employee withId(long id) {
        return new Employee(id, firstName, lastName, dateOfBirth, salary, joinDate, department);
    }
}
