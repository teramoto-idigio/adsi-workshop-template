package com.example.attendance.attendance;

import com.example.attendance.config.SecurityConfig;
import com.example.attendance.entity.Department;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.Role;
import com.example.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagerAttendanceController.class)
@Import(SecurityConfig.class)
class ManagerAttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceService attendanceService;

    @MockitoBean
    private AttendanceSummaryService summaryService;

    @MockitoBean
    private AttendanceRecordRepository attendanceRecordRepository;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("GET /api/manager/attendance: マネージャーが自部署のサマリーを取得できる")
    @WithMockUser(username = "manager@example.com", roles = "MANAGER")
    void getDepartmentSummary_asManager_returns200() throws Exception {
        var dept = Department.builder().id(2L).name("営業部").build();
        var manager = Employee.builder().id(3L).email("manager@example.com")
                .role(Role.MANAGER).department(dept).active(true).build();
        when(employeeRepository.findByEmailAndActiveTrue("manager@example.com")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByDepartmentIdAndActiveTrue(2L)).thenReturn(List.of(manager));
        when(attendanceRecordRepository.findByEmployeeIdAndDateBetween(anyLong(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/manager/attendance").param("year", "2026").param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/manager/attendance/{id}: 他部署メンバーは403")
    @WithMockUser(username = "manager@example.com", roles = "MANAGER")
    void getMemberAttendance_otherDepartment_returns403() throws Exception {
        var dept1 = Department.builder().id(1L).name("開発部").build();
        var dept2 = Department.builder().id(2L).name("営業部").build();
        var manager = Employee.builder().id(3L).email("manager@example.com")
                .role(Role.MANAGER).department(dept2).active(true).build();
        var otherMember = Employee.builder().id(1L).email("dev@example.com")
                .role(Role.EMPLOYEE).department(dept1).active(true).build();

        when(employeeRepository.findByEmailAndActiveTrue("manager@example.com")).thenReturn(Optional.of(manager));
        when(employeeRepository.findByIdWithDepartment(1L)).thenReturn(Optional.of(otherMember));

        mockMvc.perform(get("/api/manager/attendance/1").param("year", "2026").param("month", "7"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/manager/attendance: EMPLOYEE権限では403")
    @WithMockUser(username = "dev@example.com", roles = "EMPLOYEE")
    void getDepartmentSummary_asEmployee_returns403() throws Exception {
        mockMvc.perform(get("/api/manager/attendance").param("year", "2026").param("month", "7"))
                .andExpect(status().isForbidden());
    }

}
