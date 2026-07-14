package com.example.attendance.employee;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.entity.Department;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.Role;
import com.example.attendance.repository.DepartmentRepository;
import com.example.attendance.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private EmployeeServiceImpl employeeService;

    private Department department;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(employeeRepository, departmentRepository, passwordEncoder);
        department = Department.builder().id(1L).name("開発部").build();
    }

    @Test
    @DisplayName("社員登録: 正常に登録できパスワードがハッシュ化される")
    void create_validRequest_hashesPasswordAndSaves() {
        var request = new EmployeeCreateRequest("田中太郎", "tanaka@example.com", "password123", "EMPLOYEE", 1L);
        when(employeeRepository.existsByEmail("tanaka@example.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$hashed");
        when(employeeRepository.save(any())).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        var result = employeeService.create(request);

        assertThat(result.name()).isEqualTo("田中太郎");
        assertThat(result.email()).isEqualTo("tanaka@example.com");
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("社員登録: 重複メールで登録するとBusinessExceptionが発生する")
    void create_duplicateEmail_throwsBusinessException() {
        var request = new EmployeeCreateRequest("田中太郎", "tanaka@example.com", "password123", "EMPLOYEE", 1L);
        when(employeeRepository.existsByEmail("tanaka@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("このメールアドレスは既に使用されています");
    }

    @Test
    @DisplayName("社員無効化: active=falseに設定される")
    void deactivate_existingEmployee_setsActiveFalse() {
        var employee = Employee.builder().id(1L).name("田中").active(true).department(department).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);

        employeeService.deactivate(1L);

        assertThat(employee.isActive()).isFalse();
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("社員検索: 存在しないIDで検索するとResourceNotFoundExceptionが発生する")
    void findById_notFound_throwsResourceNotFoundException() {
        when(employeeRepository.findByIdWithDepartment(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("社員更新: 部署異動が正しく反映される")
    void update_changeDepartment_updatesSuccessfully() {
        var newDept = Department.builder().id(2L).name("営業部").build();
        var employee = Employee.builder().id(1L).name("田中").email("tanaka@example.com")
                .role(Role.EMPLOYEE).department(department).active(true).build();
        var request = new EmployeeUpdateRequest("田中太郎", "tanaka@example.com", "EMPLOYEE", 2L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(newDept));
        when(employeeRepository.save(any())).thenReturn(employee);

        var result = employeeService.update(1L, request);

        assertThat(employee.getDepartment().getId()).isEqualTo(2L);
    }
}
