

package com.example.attendance.attendance;

import com.example.attendance.entity.AttendanceRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceSummaryServiceTest {

    private AttendanceSummaryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AttendanceSummaryServiceImpl();
    }

    @Test
    @DisplayName("勤務時間計算: 9:00-18:00, 休憩60分 → 480分")
    void calculateWorkMinutes_normalDay_returns480() {
        var record = buildRecord(LocalTime.of(9, 0), LocalTime.of(18, 0), 60);
        assertThat(service.calculateWorkMinutes(record)).isEqualTo(480);
    }

    @Test
    @DisplayName("勤務時間計算: 9:00-20:00, 休憩60分 → 600分")
    void calculateWorkMinutes_overtime_returns600() {
        var record = buildRecord(LocalTime.of(9, 0), LocalTime.of(20, 0), 60);
        assertThat(service.calculateWorkMinutes(record)).isEqualTo(600);
    }

    @Test
    @DisplayName("勤務時間計算: clockOut未入力 → 0分")
    void calculateWorkMinutes_noClockOut_returns0() {
        var record = buildRecord(LocalTime.of(9, 0), null, 60);
        assertThat(service.calculateWorkMinutes(record)).isEqualTo(0);
    }

    @Test
    @DisplayName("残業時間計算: 480分(8h) → 残業0分")
    void calculateOvertimeMinutes_noOvertime_returns0() {
        assertThat(service.calculateOvertimeMinutes(480)).isEqualTo(0);
    }

    @Test
    @DisplayName("残業時間計算: 510分(8.5h) → 残業0分")
    void calculateOvertimeMinutes_exactPrescribed_returns0() {
        assertThat(service.calculateOvertimeMinutes(510)).isEqualTo(0);
    }

    @Test
    @DisplayName("残業時間計算: 600分(10h) → 残業90分")
    void calculateOvertimeMinutes_withOvertime_returns90() {
        assertThat(service.calculateOvertimeMinutes(600)).isEqualTo(90);
    }

    @Test
    @DisplayName("深夜勤務計算: 9:00-18:00 → 深夜0分")
    void calculateNightMinutes_dayWork_returns0() {
        var record = buildRecord(LocalTime.of(9, 0), LocalTime.of(18, 0), 60);
        assertThat(service.calculateNightMinutes(record)).isEqualTo(0);
    }

    @Test
    @DisplayName("深夜勤務計算: 18:00-23:30, 休憩0分 → 深夜90分(22:00-23:30)")
    void calculateNightMinutes_eveningWork_returns90() {
        var record = buildRecord(LocalTime.of(18, 0), LocalTime.of(23, 30), 0);
        assertThat(service.calculateNightMinutes(record)).isEqualTo(90);
    }

    @Test
    @DisplayName("深夜勤務計算: 20:00-23:00 → 深夜60分(22:00-23:00)")
    void calculateNightMinutes_partialNight_returns60() {
        var record = buildRecord(LocalTime.of(20, 0), LocalTime.of(23, 0), 0);
        assertThat(service.calculateNightMinutes(record)).isEqualTo(60);
    }

    @Test
    @DisplayName("深夜勤務計算: 22:00-翌2:00 → 深夜240分")
    void calculateNightMinutes_crossMidnight_returns240() {
        var record = buildRecord(LocalTime.of(22, 0), LocalTime.of(2, 0), 0);
        assertThat(service.calculateNightMinutes(record)).isEqualTo(240);
    }

    @Test
    @DisplayName("深夜勤務計算: clockOut未入力 → 0分")
    void calculateNightMinutes_noClockOut_returns0() {
        var record = buildRecord(LocalTime.of(9, 0), null, 0);
        assertThat(service.calculateNightMinutes(record)).isEqualTo(0);
    }

    @Test
    @DisplayName("所定労働時間計算: 2026年7月(営業日23日) → 11730分")
    void calculatePrescribedMinutes_july2026() {
        int result = service.calculatePrescribedMinutes(YearMonth.of(2026, 7));
        assertThat(result).isEqualTo(23 * 510);
    }

    @Test
    @DisplayName("月次集計: 複数日の合計が正しいこと")
    void calculateMonthlySummary_multipleRecords() {
        var records = List.of(
                buildRecord(LocalDate.of(2026, 7, 1), LocalTime.of(9, 0), LocalTime.of(18, 0), 60),
                buildRecord(LocalDate.of(2026, 7, 2), LocalTime.of(9, 0), LocalTime.of(20, 0), 60)
        );
        var summary = service.calculateMonthlySummary(records, YearMonth.of(2026, 7));

        assertThat(summary.totalWorkMinutes()).isEqualTo(480 + 600);
        assertThat(summary.totalOvertimeMinutes()).isEqualTo(0 + 90);
        assertThat(summary.totalNightMinutes()).isEqualTo(0);
    }

    private AttendanceRecord buildRecord(LocalTime clockIn, LocalTime clockOut, int breakMinutes) {
        return AttendanceRecord.builder()
                .clockIn(clockIn)
                .clockOut(clockOut)
                .breakMinutes(breakMinutes)
                .build();
    }

    private AttendanceRecord buildRecord(LocalDate date, LocalTime clockIn, LocalTime clockOut, int breakMinutes) {
        return AttendanceRecord.builder()
                .date(date)
                .clockIn(clockIn)
                .clockOut(clockOut)
                .breakMinutes(breakMinutes)
                .build();
    }
}
