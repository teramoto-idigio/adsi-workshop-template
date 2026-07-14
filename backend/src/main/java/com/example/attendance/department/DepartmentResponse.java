package com.example.attendance.department;

import com.example.attendance.entity.Department;

public record DepartmentResponse(Long id, String name, long employeeCount) {
    public static DepartmentResponse from(Department department, long employeeCount) {
        return new DepartmentResponse(department.getId(), department.getName(), employeeCount);
    }
}
