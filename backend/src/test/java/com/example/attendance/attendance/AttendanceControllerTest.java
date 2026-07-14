package com.example.attendance.attendance;

import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.attendance.dto.MonthlySummaryResponse;
import com.example.attendance.config.SecurityConfig;
import com.example.attendance.entity.Department;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.Role;
import com.example.attendance.repository.EmployeeRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendanceController.class)
@Import(SecurityConfig.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttendanceService attendanceService;

    @MockitoBean
    private AttendanceSummaryService summaryService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    private Employee mockEmployee() {
        var dept = Department.builder().id(1L).name("開発部").build();
        return Employee.builder().id(1L).email("dev@example.com").name("開発花子")
                .role(Role.EMPLOYEE).department(dept).active(true).build();
    }

    @Test
    @DisplayName("GET /api/attendance: 認証済みユーザーの月次勤怠が取得できる")
    @WithMockUser(username = "dev@example.com", roles = "EMPLOYEE")
    void getMonthly_authenticated_returns200() throws Exception {
        var emp = mockEmployee();
        when(employeeRepository.findByEmailAndActiveTrue("dev@example.com")).thenReturn(Optional.of(emp));
        var summary = new MonthlySummaryResponse(480, 0, 0, 11730, -11250);
        var response = new MonthlyAttendanceResponse(2026, 7, List.of(), summary);
        when(attendanceService.getMonthly(eq(1L), any())).thenReturn(response);

        mockMvc.perform(get("/api/attendance").param("year", "2026").param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(7));
    }

    @Test
    @DisplayName("PUT /api/attendance/{date}: 勤怠登録が成功する")
    @WithMockUser(username = "dev@example.com", roles = "EMPLOYEE")
    void upsert_validRequest_returns200() throws Exception {
        var emp = mockEmployee();
        when(employeeRepository.findByEmailAndActiveTrue("dev@example.com")).thenReturn(Optional.of(emp));
        var recordResponse = new AttendanceRecordResponse(1L, LocalDate.of(2026, 7, 14), "09:00", "18:00", 60, null, 480, 0, 0);
        when(attendanceService.upsert(eq(1L), any(), any())).thenReturn(recordResponse);

        var body = """
                {"clockIn":"09:00","clockOut":"18:00","breakMinutes":60,"note":null}
                """;

        mockMvc.perform(put("/api/attendance/2026-07-14")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workMinutes").value(480));
    }

    @Test
    @DisplayName("GET /api/attendance: 未認証は401")
    void getMonthly_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/attendance").param("year", "2026").param("month", "7"))
                .andExpect(status().isUnauthorized());
    }
}
