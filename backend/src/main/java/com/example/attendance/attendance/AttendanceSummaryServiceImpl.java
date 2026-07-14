package com.example.attendance.attendance;

import com.example.attendance.attendance.dto.MonthlySummaryResponse;
import com.example.attendance.entity.AttendanceRecord;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AttendanceSummaryServiceImpl implements AttendanceSummaryService {

    private static final int PRESCRIBED_MINUTES_PER_DAY = 510;
    private static final LocalTime NIGHT_START = LocalTime.of(22, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(5, 0);

    @Override
    public int calculateWorkMinutes(AttendanceRecord record) {
        if (record.getClockOut() == null) {
            return 0;
        }
        long totalMinutes;
        if (record.getClockOut().isBefore(record.getClockIn())) {
            totalMinutes = ChronoUnit.MINUTES.between(record.getClockIn(), LocalTime.MIDNIGHT)
                    + ChronoUnit.MINUTES.between(LocalTime.MIDNIGHT, record.getClockOut())
                    + 24 * 60;
        } else {
            totalMinutes = ChronoUnit.MINUTES.between(record.getClockIn(), record.getClockOut());
        }
        return (int) Math.max(0, totalMinutes - record.getBreakMinutes());
    }

    @Override
    public int calculateOvertimeMinutes(int workMinutes) {
        return Math.max(0, workMinutes - PRESCRIBED_MINUTES_PER_DAY);
    }

    @Override
    public int calculateNightMinutes(AttendanceRecord record) {
        if (record.getClockOut() == null) {
            return 0;
        }

        LocalTime clockIn = record.getClockIn();
        LocalTime clockOut = record.getClockOut();
        boolean crossesMidnight = clockOut.isBefore(clockIn);

        int nightMinutes = 0;

        if (crossesMidnight) {
            // e.g. 22:00 - 02:00
            // Night portion before midnight: max(clockIn, 22:00) to 24:00
            if (clockIn.isBefore(NIGHT_START)) {
                nightMinutes += (int) ChronoUnit.MINUTES.between(NIGHT_START, LocalTime.of(23, 59)) + 1;
            } else {
                nightMinutes += (int) ChronoUnit.MINUTES.between(clockIn, LocalTime.of(23, 59)) + 1;
            }
            // Night portion after midnight: 00:00 to min(clockOut, 05:00)
            LocalTime endAfterMidnight = clockOut.isAfter(NIGHT_END) ? NIGHT_END : clockOut;
            nightMinutes += (int) ChronoUnit.MINUTES.between(LocalTime.MIDNIGHT, endAfterMidnight);
        } else {
            // Same day: overlap between [clockIn, clockOut] and [22:00, 24:00)
            if (clockOut.isAfter(NIGHT_START)) {
                LocalTime effectiveStart = clockIn.isAfter(NIGHT_START) ? clockIn : NIGHT_START;
                nightMinutes = (int) ChronoUnit.MINUTES.between(effectiveStart, clockOut);
            }
        }

        return Math.max(0, nightMinutes);
    }

    @Override
    public int calculatePrescribedMinutes(YearMonth yearMonth) {
        int businessDays = 0;
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            DayOfWeek dow = yearMonth.atDay(day).getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                businessDays++;
            }
        }
        return businessDays * PRESCRIBED_MINUTES_PER_DAY;
    }

    @Override
    public MonthlySummaryResponse calculateMonthlySummary(List<AttendanceRecord> records, YearMonth yearMonth) {
        int totalWork = 0;
        int totalOvertime = 0;
        int totalNight = 0;

        for (AttendanceRecord record : records) {
            int work = calculateWorkMinutes(record);
            totalWork += work;
            totalOvertime += calculateOvertimeMinutes(work);
            totalNight += calculateNightMinutes(record);
        }

        int prescribed = calculatePrescribedMinutes(yearMonth);
        int balance = totalWork - prescribed;

        return new MonthlySummaryResponse(totalWork, totalOvertime, totalNight, prescribed, balance);
    }
}
