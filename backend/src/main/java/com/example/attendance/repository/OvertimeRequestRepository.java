package com.example.attendance.repository;

import com.example.attendance.entity.ApprovalStatus;
import com.example.attendance.entity.OvertimeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long> {
    List<OvertimeRequest> findByEmployeeId(Long employeeId);
    List<OvertimeRequest> findByEmployeeIdAndStatus(Long employeeId, ApprovalStatus status);
}
