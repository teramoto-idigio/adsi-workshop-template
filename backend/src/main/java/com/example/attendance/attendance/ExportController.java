package com.example.attendance.attendance;

import com.example.attendance.attendance.dto.EmployeeMonthlySummary;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.repository.EmployeeRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final AttendanceService attendanceService;
    private final AttendanceSummaryService summaryService;
    private final CsvExportService csvExportService;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;

    public ExportController(AttendanceService attendanceService,
                             AttendanceSummaryService summaryService,
                             CsvExportService csvExportService,
                             AttendanceRecordRepository attendanceRecordRepository,
                             EmployeeRepository employeeRepository) {
        this.attendanceService = attendanceService;
        this.summaryService = summaryService;
        this.csvExportService = csvExportService;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/attendance")
    public ResponseEntity<byte[]> exportPersonalCsv(@RequestParam int year, @RequestParam int month) {
        var employee = getCurrentEmployee();
        var yearMonth = YearMonth.of(year, month);
        var monthly = attendanceService.getMonthly(employee.getId(), yearMonth);
        var csv = csvExportService.generatePersonalCsv(monthly.records());
        return buildCsvResponse(csv, String.format("attendance_%d_%02d.csv", year, month));
    }

    @GetMapping("/department-summary")
    public ResponseEntity<byte[]> exportDepartmentSummaryCsv(@RequestParam int year, @RequestParam int month) {
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

        var csv = csvExportService.generateDepartmentSummaryCsv(summaries);
        return buildCsvResponse(csv, String.format("department_summary_%d_%02d.csv", year, month));
    }

    private ResponseEntity<byte[]> buildCsvResponse(String csv, String filename) {
        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    private Employee getCurrentEmployee() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return employeeRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new IllegalStateException("ログインユーザーが見つかりません"));
    }
}
