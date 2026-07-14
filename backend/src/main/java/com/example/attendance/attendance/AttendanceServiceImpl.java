package com.example.attendance.attendance;

import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.AttendanceRequest;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceSummaryService summaryService;

    public AttendanceServiceImpl(AttendanceRecordRepository attendanceRecordRepository,
                                  EmployeeRepository employeeRepository,
                                  AttendanceSummaryService summaryService) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeRepository = employeeRepository;
        this.summaryService = summaryService;
    }

    @Override
    public AttendanceRecordResponse upsert(Long employeeId, LocalDate date, AttendanceRequest request) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("社員が見つかりません"));

        var record = attendanceRecordRepository.findByEmployeeIdAndDate(employeeId, date)
                .orElse(AttendanceRecord.builder()
                        .employee(employee)
                        .date(date)
                        .build());

        record.setClockIn(LocalTime.parse(request.clockIn()));
        record.setClockOut(request.clockOut() != null ? LocalTime.parse(request.clockOut()) : null);
        record.setBreakMinutes(request.breakMinutes());
        record.setNote(request.note());

        var saved = attendanceRecordRepository.save(record);

        int work = summaryService.calculateWorkMinutes(saved);
        int overtime = summaryService.calculateOvertimeMinutes(work);
        int night = summaryService.calculateNightMinutes(saved);

        return AttendanceRecordResponse.from(saved, work, overtime, night);
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyAttendanceResponse getMonthly(Long employeeId, YearMonth yearMonth) {
        var start = yearMonth.atDay(1);
        var end = yearMonth.atEndOfMonth();
        var records = attendanceRecordRepository.findByEmployeeIdAndDateBetween(employeeId, start, end);

        var responses = records.stream().map(r -> {
            int work = summaryService.calculateWorkMinutes(r);
            int overtime = summaryService.calculateOvertimeMinutes(work);
            int night = summaryService.calculateNightMinutes(r);
            return AttendanceRecordResponse.from(r, work, overtime, night);
        }).toList();

        var summary = summaryService.calculateMonthlySummary(records, yearMonth);

        return new MonthlyAttendanceResponse(yearMonth.getYear(), yearMonth.getMonthValue(), responses, summary);
    }
}
