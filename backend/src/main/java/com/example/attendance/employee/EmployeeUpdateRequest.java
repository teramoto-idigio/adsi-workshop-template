package com.example.attendance.employee;

import jakarta.validation.constraints.*;

public record EmployeeUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email String email,
        @NotBlank String role,
        @NotNull Long departmentId
) {
}
