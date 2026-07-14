package com.example.attendance.employee;

import jakarta.validation.constraints.*;

public record EmployeeCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String role,
        @NotNull Long departmentId
) {
}
