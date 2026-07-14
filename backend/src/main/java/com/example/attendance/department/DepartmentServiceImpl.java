package com.example.attendance.department;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.entity.Department;
import com.example.attendance.repository.DepartmentRepository;
import com.example.attendance.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                  EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.name())) {
            throw new BusinessException("この部署名は既に使用されています");
        }
        var department = Department.builder().name(request.name()).build();
        var saved = departmentRepository.save(department);
        return DepartmentResponse.from(saved, 0);
    }

    @Override
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        var department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("部署が見つかりません"));
        department.setName(request.name());
        var saved = departmentRepository.save(department);
        long count = employeeRepository.countByDepartmentId(id);
        return DepartmentResponse.from(saved, count);
    }

    @Override
    public void delete(Long id) {
        var department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("部署が見つかりません"));
        long count = employeeRepository.countByDepartmentId(id);
        if (count > 0) {
            throw new BusinessException("所属社員がいるため削除できません");
        }
        departmentRepository.delete(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll().stream()
                .map(d -> DepartmentResponse.from(d, employeeRepository.countByDepartmentId(d.getId())))
                .toList();
    }
}
