package com.example.attendance.department;

import com.example.attendance.common.exception.BusinessException;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
@Import(SecurityConfig.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DepartmentService departmentService;

    @Test
    @DisplayName("POST /api/admin/departments: ADMIN権限で部署作成すると201が返る")
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_returns201() throws Exception {
        var response = new DepartmentResponse(1L, "開発部", 0);
        when(departmentService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/admin/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DepartmentRequest("開発部"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("開発部"));
    }

    @Test
    @DisplayName("POST /api/admin/departments: EMPLOYEE権限でアクセスすると403が返る")
    @WithMockUser(roles = "EMPLOYEE")
    void create_asEmployee_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DepartmentRequest("開発部"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/admin/departments/{id}: 所属社員ありで削除すると409が返る")
    @WithMockUser(roles = "ADMIN")
    void delete_hasEmployees_returns409() throws Exception {
        doThrow(new BusinessException("所属社員がいるため削除できません"))
                .when(departmentService).delete(1L);

        mockMvc.perform(delete("/api/admin/departments/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("所属社員がいるため削除できません"));
    }

    @Test
    @DisplayName("GET /api/admin/departments: 部署一覧が返る")
    @WithMockUser(roles = "ADMIN")
    void getAll_returnsList() throws Exception {
        when(departmentService.findAll()).thenReturn(List.of(
                new DepartmentResponse(1L, "開発部", 3),
                new DepartmentResponse(2L, "営業部", 5)
        ));

        mockMvc.perform(get("/api/admin/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("未認証でアクセスすると401が返る")
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/departments"))
                .andExpect(status().isUnauthorized());
    }
}
