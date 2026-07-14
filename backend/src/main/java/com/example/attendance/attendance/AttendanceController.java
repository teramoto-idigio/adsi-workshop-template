package com.example.attendance.attendance;

import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.AttendanceRequest;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.attendance.dto.MonthlySummaryResponse;
import com.example.attendance.repository.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceSummaryService summaryService;
    private final EmployeeRepository employeeRepository;

    public AttendanceController(AttendanceService attendanceService,
                                 AttendanceSummaryService summaryService,
                                 EmployeeRepository employeeRepository) {
        this.attendanceService = attendanceService;
        this.summaryService = summaryService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public ResponseEntity<MonthlyAttendanceResponse> getMonthly(
            @RequestParam int year, @RequestParam int month) {
        Long employeeId = getCurrentEmployeeId();
        var response = attendanceService.getMonthly(employeeId, YearMonth.of(year, month));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{date}")
    public ResponseEntity<AttendanceRecordResponse> upsert(
            @PathVariable LocalDate date, @Valid @RequestBody AttendanceRequest request) {
        Long employeeId = getCurrentEmployeeId();
        var response = attendanceService.upsert(employeeId, date, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryResponse> getSummary(
            @RequestParam int year, @RequestParam int month) {
        Long employeeId = getCurrentEmployeeId();
        var monthly = attendanceService.getMonthly(employeeId, YearMonth.of(year, month));
        return ResponseEntity.ok(monthly.summary());
    }

    private Long getCurrentEmployeeId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return employeeRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new IllegalStateException("ログインユーザーが見つかりません"))
                .getId();
    }
}
