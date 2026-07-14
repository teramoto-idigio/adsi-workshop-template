package com.example.attendance.attendance.dto;

import jakarta.validation.constraints.NotNull;

public record AttendanceRequest(
        @NotNull String clockIn,
        String clockOut,
        @NotNull Integer breakMinutes,
        String note
) {
}
