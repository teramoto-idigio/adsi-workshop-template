package com.example.attendance.employee;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse create(EmployeeCreateRequest request);
    EmployeeResponse update(Long id, EmployeeUpdateRequest request);
    void deactivate(Long id);
    List<EmployeeResponse> findAll(Long departmentId, Boolean active);
    EmployeeResponse findById(Long id);
}
