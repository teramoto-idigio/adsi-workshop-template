package com.example.attendance.attendance.dto;

public record EmployeeMonthlySummary(
        Long employeeId,
        String employeeName,
        int totalWorkMinutes,
        int totalOvertimeMinutes,
        int totalNightMinutes,
        int workDays
) {
}
