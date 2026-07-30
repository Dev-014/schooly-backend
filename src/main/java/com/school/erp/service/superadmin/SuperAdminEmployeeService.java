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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuperAdminEmployeeService {

    private final UserRepository userRepository;
    private final UserSchoolRoleRepository userSchoolRoleRepository;
    private final SupportTicketRepository supportTicketRepository;

    @Transactional(readOnly = true)
    public List<SuperAdminEmployeeDTO> getAllEmployees() {
        List<UserSchoolRole> superAdminRoles = userSchoolRoleRepository.findByRole(UserRole.SUPER_ADMIN);
        
        return superAdminRoles.stream()
                .map(role -> {
                    User user = role.getUser();
                    SuperAdminEmployeeDTO dto = new SuperAdminEmployeeDTO();
                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setEmail(user.getEmail());
                    dto.setPhone(user.getPhone());
                    dto.setStatus(user.getStatus());
                    dto.setJoinedAt(role.getJoinedAt() != null ? role.getJoinedAt().toString() : null);
                    dto.setActiveTickets((int) supportTicketRepository.countActiveTicketsByEmployee(user.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public SuperAdminEmployeeDTO createEmployee(CreateSuperAdminEmployeeRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(request.getPassword()); // TODO: hash with BCrypt
        user.setStatus("ACTIVE");
        user = userRepository.save(user);

        UserSchoolRole role = new UserSchoolRole();
        role.setUser(user);
        role.setSchool(null); // Global role
        role.setRole(UserRole.SUPER_ADMIN);
        role.setStatus("ACTIVE");
        userSchoolRoleRepository.save(role);

        SuperAdminEmployeeDTO dto = new SuperAdminEmployeeDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setActiveTickets(0);
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

        SuperAdminEmployeeDTO dto = new SuperAdminEmployeeDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setActiveTickets((int) supportTicketRepository.countActiveTicketsByEmployee(user.getId()));
        return dto;
    }
}
