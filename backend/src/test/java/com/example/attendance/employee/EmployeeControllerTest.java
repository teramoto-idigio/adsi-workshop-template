package com.example.attendance.employee;

import com.example.attendance.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("POST /api/admin/employees: ADMIN権限で社員登録すると201が返る")
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_returns201() throws Exception {
        var response = new EmployeeResponse(1L, "田中太郎", "tanaka@example.com", "EMPLOYEE", 1L, "開発部", true);
        when(employeeService.create(any())).thenReturn(response);

        var request = new EmployeeCreateRequest("田中太郎", "tanaka@example.com", "password123", "EMPLOYEE", 1L);
        mockMvc.perform(post("/api/admin/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("田中太郎"));
    }

    @Test
    @DisplayName("POST /api/admin/employees: MANAGER権限ではアクセスできない")
    @WithMockUser(roles = "MANAGER")
    void create_asManager_returns403() throws Exception {
        var request = new EmployeeCreateRequest("田中太郎", "tanaka@example.com", "password123", "EMPLOYEE", 1L);
        mockMvc.perform(post("/api/admin/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/employees: 社員一覧が返る")
    @WithMockUser(roles = "ADMIN")
    void getAll_returnsList() throws Exception {
        when(employeeService.findAll(null, true)).thenReturn(List.of(
                new EmployeeResponse(1L, "田中太郎", "tanaka@example.com", "EMPLOYEE", 1L, "開発部", true)
        ));

        mockMvc.perform(get("/api/admin/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("POST /api/admin/employees: バリデーションエラー（メール不正）で400が返る")
    @WithMockUser(roles = "ADMIN")
    void create_invalidEmail_returns400() throws Exception {
        var request = new EmployeeCreateRequest("田中太郎", "not-email", "password123", "EMPLOYEE", 1L);
        mockMvc.perform(post("/api/admin/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
