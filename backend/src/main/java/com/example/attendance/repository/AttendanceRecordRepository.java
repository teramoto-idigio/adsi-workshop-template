package com.example.attendance.repository;

import com.example.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate start, LocalDate end);
    Optional<AttendanceRecord> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
}
