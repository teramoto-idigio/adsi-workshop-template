package com.example.attendance.attendance.dto;

import com.example.attendance.entity.AttendanceRecord;
import java.time.LocalDate;

public record AttendanceRecordResponse(
        Long id,
        LocalDate date,
        String clockIn,
        String clockOut,
        Integer breakMinutes,
        String note,
        Integer workMinutes,
        Integer overtimeMinutes,
        Integer nightMinutes
) {
    public static AttendanceRecordResponse from(AttendanceRecord record, int workMinutes, int overtimeMinutes, int nightMinutes) {
        return new AttendanceRecordResponse(
                record.getId(),
                record.getDate(),
                record.getClockIn().toString(),
                record.getClockOut() != null ? record.getClockOut().toString() : null,
                record.getBreakMinutes(),
                record.getNote(),
                workMinutes,
                overtimeMinutes,
                nightMinutes
        );
    }
}
