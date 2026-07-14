package com.example.attendance.department;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse create(DepartmentRequest request);
    DepartmentResponse update(Long id, DepartmentRequest request);
    void delete(Long id);
    List<DepartmentResponse> findAll();
}
