package com.example.attendance.repository;

import com.example.attendance.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);

    @Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.email = :email AND e.active = true")
    Optional<Employee> findByEmailAndActiveTrue(@Param("email") String email);

    @Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.department.id = :departmentId AND e.active = true")
    List<Employee> findByDepartmentIdAndActiveTrue(@Param("departmentId") Long departmentId);

    @Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.active = true")
    List<Employee> findByActiveTrue();

    @Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.id = :id")
    Optional<Employee> findByIdWithDepartment(@Param("id") Long id);

    boolean existsByEmail(String email);
    long countByDepartmentId(Long departmentId);
}
