package com.example.attendance.department;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.entity.Department;
import com.example.attendance.repository.DepartmentRepository;
import com.example.attendance.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private DepartmentServiceImpl departmentService;

    @BeforeEach
    void setUp() {
        departmentService = new DepartmentServiceImpl(departmentRepository, employeeRepository);
    }

    @Test
    @DisplayName("部署作成: 新しい部署名で作成すると成功する")
    void create_newName_succeeds() {
        var request = new DepartmentRequest("開発部");
        var saved = Department.builder().id(1L).name("開発部").build();
        when(departmentRepository.existsByName("開発部")).thenReturn(false);
        when(departmentRepository.save(any())).thenReturn(saved);

        var result = departmentService.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("開発部");
        assertThat(result.employeeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("部署作成: 既存の部署名で作成するとBusinessExceptionが発生する")
    void create_duplicateName_throwsBusinessException() {
        var request = new DepartmentRequest("開発部");
        when(departmentRepository.existsByName("開発部")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("この部署名は既に使用されています");
    }

    @Test
    @DisplayName("部署削除: 所属社員がいない部署を削除すると成功する")
    void delete_noEmployees_succeeds() {
        var department = Department.builder().id(1L).name("開発部").build();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(0L);

        departmentService.delete(1L);

        verify(departmentRepository).delete(department);
    }

    @Test
    @DisplayName("部署削除: 所属社員がいる部署を削除するとBusinessExceptionが発生する")
    void delete_hasEmployees_throwsBusinessException() {
        var department = Department.builder().id(1L).name("開発部").build();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(5L);

        assertThatThrownBy(() -> departmentService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("所属社員がいるため削除できません");
    }

    @Test
    @DisplayName("部署削除: 存在しないIDで削除するとResourceNotFoundExceptionが発生する")
    void delete_notFound_throwsResourceNotFoundException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("部署一覧: 全部署が社員数付きで返される")
    void findAll_returnsDepartmentsWithCount() {
        var dept1 = Department.builder().id(1L).name("開発部").build();
        var dept2 = Department.builder().id(2L).name("営業部").build();
        when(departmentRepository.findAll()).thenReturn(List.of(dept1, dept2));
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(3L);
        when(employeeRepository.countByDepartmentId(2L)).thenReturn(5L);

        var result = departmentService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).employeeCount()).isEqualTo(3);
        assertThat(result.get(1).employeeCount()).isEqualTo(5);
    }
}
