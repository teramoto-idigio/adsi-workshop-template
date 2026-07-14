package com.example.attendance.attendance;

import com.example.attendance.attendance.dto.EmployeeMonthlySummary;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.repository.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/manager/attendance")
public class ManagerAttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceSummaryService summaryService;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;

    public ManagerAttendanceController(AttendanceService attendanceService,
                                        AttendanceSummaryService summaryService,
                                        AttendanceRecordRepository attendanceRecordRepository,
                                        EmployeeRepository employeeRepository) {
        this.attendanceService = attendanceService;
        this.summaryService = summaryService;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeMonthlySummary>> getDepartmentSummary(
            @RequestParam int year, @RequestParam int month) {
        var manager = getCurrentEmployee();
        var members = employeeRepository.findByDepartmentIdAndActiveTrue(manager.getDepartment().getId());
        var yearMonth = YearMonth.of(year, month);

        var summaries = members.stream().map(member -> {
            var records = attendanceRecordRepository.findByEmployeeIdAndDateBetween(
                    member.getId(), yearMonth.atDay(1), yearMonth.atEndOfMonth());
            int totalWork = 0, totalOvertime = 0, totalNight = 0;
            for (var r : records) {
                int work = summaryService.calculateWorkMinutes(r);
                totalWork += work;
                totalOvertime += summaryService.calculateOvertimeMinutes(work);
                totalNight += summaryService.calculateNightMinutes(r);
            }
            return new EmployeeMonthlySummary(
                    member.getId(), member.getName(),
                    totalWork, totalOvertime, totalNight, records.size());
        }).toList();

        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<MonthlyAttendanceResponse> getMemberAttendance(
            @PathVariable Long employeeId, @RequestParam int year, @RequestParam int month) {
        var manager = getCurrentEmployee();
        var target = employeeRepository.findByIdWithDepartment(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません"));

        if (!target.getDepartment().getId().equals(manager.getDepartment().getId())) {
            return ResponseEntity.status(403).build();
        }

        var response = attendanceService.getMonthly(employeeId, YearMonth.of(year, month));
        return ResponseEntity.ok(response);
    }

    private Employee getCurrentEmployee() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return employeeRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new IllegalStateException("ログインユーザーが見つかりません"));
    }
}
