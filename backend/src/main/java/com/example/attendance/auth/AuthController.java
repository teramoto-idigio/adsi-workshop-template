package com.example.attendance.auth;

import com.example.attendance.employee.EmployeeResponse;
import com.example.attendance.repository.EmployeeRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmployeeRepository employeeRepository;

    public AuthController(AuthService authService, EmployeeRepository employeeRepository) {
        this.authService = authService;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        try {
            var employee = authService.authenticate(request.email(), request.password());
            var auth = new UsernamePasswordAuthenticationToken(
                    employee.email(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + employee.role()))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            return ResponseEntity.ok(new LoginResponse(employee));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(new com.example.attendance.common.dto.ErrorResponse(
                            "メールアドレスまたはパスワードが正しくありません", "UNAUTHORIZED"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<EmployeeResponse> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        var email = (String) auth.getPrincipal();
        var employee = employeeRepository.findByEmailAndActiveTrue(email)
                .map(EmployeeResponse::from)
                .orElse(null);
        if (employee == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(employee);
    }
}
