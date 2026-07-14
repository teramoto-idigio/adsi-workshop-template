package com.example.attendance.auth;

import com.example.attendance.employee.EmployeeResponse;

public interface AuthService {
    EmployeeResponse authenticate(String email, String password);
}
