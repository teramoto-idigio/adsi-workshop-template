package com.example.attendance.auth;

import com.example.attendance.config.SecurityConfig;
import com.example.attendance.employee.EmployeeResponse;
import com.example.attendance.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("POST /api/auth/login: 正しい認証情報でログイン成功すると200が返る")
    void login_validCredentials_returns200() throws Exception {
        var employeeResponse = new EmployeeResponse(1L, "田中太郎", "tanaka@example.com", "EMPLOYEE", 1L, "開発部", true);
        when(authService.authenticate("tanaka@example.com", "password123")).thenReturn(employeeResponse);

        var request = new LoginRequest("tanaka@example.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee.name").value("田中太郎"));
    }

    @Test
    @DisplayName("POST /api/auth/login: 不正な認証情報でログインすると401が返る")
    void login_invalidCredentials_returns401() throws Exception {
        when(authService.authenticate("tanaka@example.com", "wrong"))
                .thenThrow(new BadCredentialsException("メールアドレスまたはパスワードが正しくありません"));

        var request = new LoginRequest("tanaka@example.com", "wrong");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
