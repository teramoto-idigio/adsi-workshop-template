package com.example.attendance.attendance.dto;

import java.util.List;

public record MonthlyAttendanceResponse(
        int year,
        int month,
        List<AttendanceRecordResponse> records,
        MonthlySummaryResponse summary
) {
}
