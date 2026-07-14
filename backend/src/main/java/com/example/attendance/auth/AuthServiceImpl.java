package com.example.attendance.auth;

import com.example.attendance.employee.EmployeeResponse;
import com.example.attendance.repository.EmployeeRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EmployeeResponse authenticate(String email, String password) {
        var employee = employeeRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new BadCredentialsException("メールアドレスまたはパスワードが正しくありません"));

        if (!passwordEncoder.matches(password, employee.getPasswordHash())) {
            throw new BadCredentialsException("メールアドレスまたはパスワードが正しくありません");
        }

        return EmployeeResponse.from(employee);
    }
}
