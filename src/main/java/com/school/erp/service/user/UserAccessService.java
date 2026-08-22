package com.school.erp.service.user;

import com.school.erp.dto.user.GrantAccessDTO;
import com.school.erp.dto.user.GrantAccessResponseDTO;
import com.school.erp.entity.*;
import com.school.erp.repository.*;
import com.school.erp.service.superadmin.UserSupportService;
import com.school.erp.service.auth.RoleSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccessService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    // Assuming these repositories exist
    // private final StaffRepository staffRepository;
    // private final FamilyRepository familyRepository;
    private final UserSchoolRoleRepository userSchoolRoleRepository;
    private final UserSupportService userSupportService;
    private final RoleSyncService roleSyncService;

    @Transactional
    public GrantAccessResponseDTO grantAccess(GrantAccessDTO request) {
        String name = "";
        String email = null;
        String phone = null;
        String entityIdentifier = "";
        School school = null;
        UserRole roleToGrant = UserRole.STUDENT;

        if ("STUDENT".equalsIgnoreCase(request.getEntityType())) {
            Student student = studentRepository.findById(request.getEntityId())
                    .orElseThrow(() -> new IllegalArgumentException("Student not found"));
            name = student.getName();
            // In a real app, student might have email/phone on their entity or linked parent
            email = null; // Assume null for this demo unless they have a field
            phone = null;
            entityIdentifier = student.getAdmissionNo();
            school = student.getSchool();
            roleToGrant = UserRole.STUDENT;
            
            // Check if they already have a user
            if (student.getUserId() != null) {
                return GrantAccessResponseDTO.builder()
                        .success(false)
                        .message("Student already has login access.")
                        .build();
            }
        } 
        // Implement STAFF and PARENT logic here...
        else {
            throw new IllegalArgumentException("Entity type not supported yet");
        }

        // 1. Create or Find User
        User user = new User();
        user.setName(name);
        user.setStatus("ACTIVE");
        
        // Generate Username and Temp Password if no email/phone
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        String username = entityIdentifier;
        
        if (email != null) {
            user.setEmail(email);
        } else if (phone != null) {
            user.setPhone(phone);
        } else {
            // Since the database enforces phone to be NOT NULL and UNIQUE,
            // we will use the username (e.g. Admission No) as the phone placeholder.
            user.setPhone(username);
        }
        
        user.setPasswordHash("$2a$10$temporaryHashFor" + tempPassword); // Mocked bcrypt hash
        user = userRepository.save(user);

        // Link back to entity (e.g. Student)
        if ("STUDENT".equalsIgnoreCase(request.getEntityType())) {
            Student student = studentRepository.findById(request.getEntityId()).get();
            student.setUserId(user.getId());
            studentRepository.save(student);
        }

        // 2. Assign Role
        UserSchoolRole usr = new UserSchoolRole();
        usr.setUser(user);
        usr.setSchool(school);
        usr.setRole(roleToGrant);
        usr.setStatus("ACTIVE");
        userSchoolRoleRepository.save(usr);
        roleSyncService.syncUserSchoolRole(usr);

        // 3. Apply Contact Strategy
        if (email != null) {
            // Trigger password reset email
            userSupportService.sendPasswordResetLink(user.getId());
            return GrantAccessResponseDTO.builder()
                    .success(true)
                    .strategyUsed("EMAIL")
                    .message("Login access granted. A password reset link has been sent to their email.")
                    .build();
        } else if (phone != null) {
            // Trigger SMS (mocked)
            return GrantAccessResponseDTO.builder()
                    .success(true)
                    .strategyUsed("SMS")
                    .message("Login access granted. A temporary password has been sent via SMS.")
                    .build();
        } else {
            // Manual Handover
            return GrantAccessResponseDTO.builder()
                    .success(true)
                    .strategyUsed("MANUAL")
                    .message("Login access granted. Please provide these credentials to the user manually.")
                    .generatedUsername(username)
                    .generatedPassword(tempPassword)
                    .build();
        }
    }
}
