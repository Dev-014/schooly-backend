package com.school.erp.service.superadmin;

import com.school.erp.dto.CreateDepartmentRequest;
import com.school.erp.dto.DepartmentDTO;
import com.school.erp.entity.SuperAdminEmployee;
import com.school.erp.entity.superadmin.Department;
import com.school.erp.repository.SuperAdminEmployeeRepository;
import com.school.erp.repository.superadmin.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final SuperAdminEmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentDTO createDepartment(CreateDepartmentRequest request) {
        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());

        if (request.getHeadEmployeeId() != null) {
            SuperAdminEmployee head = employeeRepository.findById(request.getHeadEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Head employee not found"));
            department.setHeadEmployee(head);
        }

        Department savedDepartment = departmentRepository.save(department);
        return mapToDTO(savedDepartment);
    }

    private DepartmentDTO mapToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        
        if (department.getHeadEmployee() != null) {
            dto.setHeadEmployeeId(department.getHeadEmployee().getId());
            dto.setHeadEmployeeName(department.getHeadEmployee().getUser().getName());
        }
        
        return dto;
    }
}
