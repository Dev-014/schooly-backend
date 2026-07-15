package com.school.erp.service;

import com.school.erp.dto.onboarding.OnboardingRegisterRequest;
import com.school.erp.dto.onboarding.OnboardingRegisterResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.User;
import com.school.erp.entity.UserRole;
import com.school.erp.entity.UserSchoolRole;
import com.school.erp.exception.BadRequestException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.UserRepository;
import com.school.erp.repository.UserSchoolRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
public class OnboardingService {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final UserSchoolRoleRepository userSchoolRoleRepository;

    public OnboardingService(SchoolRepository schoolRepository, UserRepository userRepository, UserSchoolRoleRepository userSchoolRoleRepository) {
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
        this.userSchoolRoleRepository = userSchoolRoleRepository;
    }

    @Transactional
    public OnboardingRegisterResponse registerSchool(OnboardingRegisterRequest request) {
        if (schoolRepository.existsByCode(request.schoolCode())) {
            throw new BadRequestException("School with code " + request.schoolCode() + " already exists");
        }

        School school = new School();
        school.setName(request.schoolName());
        school.setCode(request.schoolCode());
        school.setContactEmail(request.contactEmail());
        school.setContactPhone(request.contactPhone());
        school.setAddress(request.address());
        school.setStatus("ACTIVE");
        if (request.metadata() != null) {
            school.setMetadata(new HashMap<>(request.metadata()));
        }
        school = schoolRepository.save(school);

        User user = userRepository.findByPhone(request.adminPhone()).orElse(null);
        if (user == null) {
            user = new User();
            user.setPhone(request.adminPhone());
            user.setName(request.adminName());
            user.setEmail(request.adminEmail());
            user.setPasswordHash(request.adminPassword() != null ? request.adminPassword() : "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2");
            user.setStatus("ACTIVE");
            if (request.metadata() != null) {
                user.setMetadata(new HashMap<>(request.metadata()));
            }
            user = userRepository.save(user);
        } else {
            user.setName(request.adminName());
            if (request.adminEmail() != null) user.setEmail(request.adminEmail());
            if (request.metadata() != null) {
                if (user.getMetadata() == null) user.setMetadata(new HashMap<>());
                user.getMetadata().putAll(request.metadata());
            }
            user = userRepository.save(user);
        }

        UserSchoolRole role = new UserSchoolRole();
        role.setUser(user);
        role.setSchool(school);
        role.setRole(UserRole.ADMIN);
        role.setStatus("ACTIVE");
        userSchoolRoleRepository.save(role);

        return new OnboardingRegisterResponse(
                school.getId(),
                school.getName(),
                school.getCode(),
                user.getId(),
                user.getPhone(),
                school.getStatus(),
                school.getMetadata()
        );
    }
}
