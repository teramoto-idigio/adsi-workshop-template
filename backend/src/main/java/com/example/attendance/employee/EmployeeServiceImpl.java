package com.example.attendance.employee;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.Role;
import com.example.attendance.repository.DepartmentRepository;
import com.example.attendance.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EmployeeResponse create(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmail(request.email())) {
            throw new BusinessException("このメールアドレスは既に使用されています");
        }

        var department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("部署が見つかりません"));

        var employee = Employee.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.valueOf(request.role()))
                .department(department)
                .active(true)
                .build();

        var saved = employeeRepository.save(employee);
        return EmployeeResponse.from(saved);
    }

    @Override
    public EmployeeResponse update(Long id, EmployeeUpdateRequest request) {
        var employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません"));

        var department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("部署が見つかりません"));

        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setRole(Role.valueOf(request.role()));
        employee.setDepartment(department);

        var saved = employeeRepository.save(employee);
        return EmployeeResponse.from(saved);
    }

    @Override
    public void deactivate(Long id) {
        var employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません"));
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll(Long departmentId, Boolean active) {
        List<Employee> employees;
        if (departmentId != null && (active == null || active)) {
            employees = employeeRepository.findByDepartmentIdAndActiveTrue(departmentId);
        } else if (active == null || active) {
            employees = employeeRepository.findByActiveTrue();
        } else {
            employees = employeeRepository.findAll();
        }
        return employees.stream().map(EmployeeResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        var employee = employeeRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません"));
        return EmployeeResponse.from(employee);
    }
}
