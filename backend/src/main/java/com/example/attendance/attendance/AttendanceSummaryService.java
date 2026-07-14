package com.example.attendance.attendance;

import com.example.attendance.attendance.dto.MonthlySummaryResponse;
import com.example.attendance.entity.AttendanceRecord;

import java.time.YearMonth;
import java.util.List;

public interface AttendanceSummaryService {
    int calculateWorkMinutes(AttendanceRecord record);
    int calculateOvertimeMinutes(int workMinutes);
    int calculateNightMinutes(AttendanceRecord record);
    int calculatePrescribedMinutes(YearMonth yearMonth);
    MonthlySummaryResponse calculateMonthlySummary(List<AttendanceRecord> records, YearMonth yearMonth);
}
