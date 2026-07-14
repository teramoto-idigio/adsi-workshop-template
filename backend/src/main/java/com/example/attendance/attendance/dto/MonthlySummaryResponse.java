package com.example.attendance.attendance.dto;

public record MonthlySummaryResponse(
        int totalWorkMinutes,
        int totalOvertimeMinutes,
        int totalNightMinutes,
        int prescribedMinutes,
        int balanceMinutes
) {
}
