package com.example.attendance.employee;

import com.example.attendance.entity.Employee;

public record EmployeeResponse(
        Long id,
        String name,
        String email,
        String role,
        Long departmentId,
        String departmentName,
        boolean active
) {
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getRole().name(),
                employee.getDepartment().getId(),
                employee.getDepartment().getName(),
                employee.isActive()
        );
    }
}
