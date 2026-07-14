package com.example.attendance.attendance;

import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.EmployeeMonthlySummary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CsvExportService {

    public String generatePersonalCsv(List<AttendanceRecordResponse> records) {
        var sb = new StringBuilder();
        sb.append("日付,出勤,退勤,休憩(分),勤務時間(分),残業(分),深夜(分),備考\n");
        for (var r : records) {
            sb.append(String.format("%s,%s,%s,%d,%d,%d,%d,%s\n",
                    r.date(),
                    r.clockIn(),
                    r.clockOut() != null ? r.clockOut() : "",
                    r.breakMinutes(),
                    r.workMinutes(),
                    r.overtimeMinutes(),
                    r.nightMinutes(),
                    r.note() != null ? r.note() : ""));
        }
        return sb.toString();
    }

    public String generateDepartmentSummaryCsv(List<EmployeeMonthlySummary> summaries) {
        var sb = new StringBuilder();
        sb.append("社員名,勤務日数,総労働時間(分),残業時間(分),深夜勤務(分)\n");
        for (var s : summaries) {
            sb.append(String.format("%s,%d,%d,%d,%d\n",
                    s.employeeName(),
                    s.workDays(),
                    s.totalWorkMinutes(),
                    s.totalOvertimeMinutes(),
                    s.totalNightMinutes()));
        }
        return sb.toString();
    }
}
