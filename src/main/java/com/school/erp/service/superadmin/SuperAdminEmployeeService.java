package com.school.erp.service.superadmin;

import com.school.erp.dto.CreateSuperAdminEmployeeRequest;
import com.school.erp.dto.SuperAdminEmployeeDTO;
import com.school.erp.dto.UpdateSuperAdminEmployeeRequest;
import com.school.erp.entity.User;
import com.school.erp.entity.UserRole;
import com.school.erp.entity.UserSchoolRole;
import com.school.erp.repository.SupportTicketRepository;
import com.school.erp.repository.UserRepository;
import com.school.erp.repository.UserSchoolRoleRepository;
import com.school.erp.service.auth.RoleSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuperAdminEmployeeService {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UserRepository userRepository;
    private final UserSchoolRoleRepository userSchoolRoleRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final com.school.erp.repository.SuperAdminEmployeeRepository employeeRepository;
    private final RoleSyncService roleSyncService;

    @Transactional(readOnly = true)
    public SuperAdminEmployeeDTO getEmployeeById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        com.school.erp.entity.SuperAdminEmployee emp = employeeRepository.findByUserId(id).orElse(new com.school.erp.entity.SuperAdminEmployee());

        SuperAdminEmployeeDTO dto = new SuperAdminEmployeeDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setJoinedAt(emp.getJoinedAt() != null ? emp.getJoinedAt().toString() : null);
        dto.setActiveTickets((int) supportTicketRepository.countActiveTicketsByEmployee(user.getId()));
        dto.setDepartment(emp.getDepartment());
        dto.setDesignation(emp.getDesignation());
        dto.setEmployeeCode(emp.getEmployeeCode());
        dto.setSalaryBand(emp.getSalaryBand());
        dto.setLeaveBalance(emp.getLeaveBalance());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<SuperAdminEmployeeDTO> getAllEmployees() {
        List<com.school.erp.entity.SuperAdminEmployee> employees = employeeRepository.findAllWithUser();
        
        return employees.stream()
                .map(emp -> {
                    User user = emp.getUser();
                    SuperAdminEmployeeDTO dto = new SuperAdminEmployeeDTO();
                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setEmail(user.getEmail());
                    dto.setPhone(user.getPhone());
                    dto.setStatus(user.getStatus());
                    dto.setJoinedAt(emp.getJoinedAt() != null ? emp.getJoinedAt().toString() : null);
                    dto.setActiveTickets((int) supportTicketRepository.countActiveTicketsByEmployee(user.getId()));
                    dto.setDepartment(emp.getDepartment());
                    dto.setDesignation(emp.getDesignation());
                    dto.setEmployeeCode(emp.getEmployeeCode());
                    dto.setSalaryBand(emp.getSalaryBand());
                    dto.setLeaveBalance(emp.getLeaveBalance());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public SuperAdminEmployeeDTO createEmployee(CreateSuperAdminEmployeeRequest request) {
        // Guard: check for duplicate email
        if (request.getEmail() != null && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("An employee with email '" + request.getEmail() + "' already exists.");
        }
        // Guard: check for duplicate phone
        if (request.getPhone() != null && userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("An employee with phone '" + request.getPhone() + "' already exists.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        // Hash the password securely with BCrypt
        String rawPassword = request.getPassword() != null ? request.getPassword() : "Welcome@123";
        user.setPasswordHash(PASSWORD_ENCODER.encode(rawPassword));
        user.setStatus("ACTIVE");
        user = userRepository.save(user);

        UserSchoolRole role = new UserSchoolRole();
        role.setUser(user);
        role.setSchool(null); // Global role
        role.setRole(UserRole.SUPER_ADMIN);
        role.setStatus("ACTIVE");
        userSchoolRoleRepository.save(role);
        roleSyncService.syncUserSchoolRole(role);

        com.school.erp.entity.SuperAdminEmployee emp = new com.school.erp.entity.SuperAdminEmployee();
        emp.setUser(user);
        emp.setJoinedAt(java.time.LocalDate.now());
        emp.setDepartment(request.getDepartment());
        emp.setDesignation(request.getDesignation());
        emp.setEmployeeCode(request.getEmployeeCode());
        emp.setSalaryBand(request.getSalaryBand());
        employeeRepository.save(emp);

        SuperAdminEmployeeDTO dto = new SuperAdminEmployeeDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setJoinedAt(emp.getJoinedAt().toString());
        dto.setActiveTickets(0);
        dto.setDepartment(emp.getDepartment());
        dto.setDesignation(emp.getDesignation());
        dto.setEmployeeCode(emp.getEmployeeCode());
        dto.setSalaryBand(emp.getSalaryBand());
        dto.setLeaveBalance(emp.getLeaveBalance());
        return dto;
    }

    @Transactional
    public SuperAdminEmployeeDTO updateEmployee(Long id, UpdateSuperAdminEmployeeRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        user = userRepository.save(user);

        com.school.erp.entity.SuperAdminEmployee emp = employeeRepository.findByUserId(id).orElse(null);
        String joinedAt = null;
        if (emp != null) {
            joinedAt = emp.getJoinedAt() != null ? emp.getJoinedAt().toString() : null;
        }

        SuperAdminEmployeeDTO dto = new SuperAdminEmployeeDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setJoinedAt(joinedAt);
        dto.setActiveTickets((int) supportTicketRepository.countActiveTicketsByEmployee(user.getId()));
        return dto;
    }
}
