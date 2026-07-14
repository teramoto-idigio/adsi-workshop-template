package com.example.attendance.attendance;

import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.AttendanceRequest;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;

import java.time.LocalDate;
import java.time.YearMonth;

public interface AttendanceService {
    AttendanceRecordResponse upsert(Long employeeId, LocalDate date, AttendanceRequest request);
    MonthlyAttendanceResponse getMonthly(Long employeeId, YearMonth yearMonth);
}
