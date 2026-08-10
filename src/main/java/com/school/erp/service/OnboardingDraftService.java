package com.school.erp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.onboarding.OnboardingDraftDTO;
import com.school.erp.dto.onboarding.OnboardingInitRequest;
import com.school.erp.dto.onboarding.OnboardingStepRequest;
import com.school.erp.entity.DataImportError;
import com.school.erp.entity.DataImportJob;
import com.school.erp.entity.OnboardingDraft;
import com.school.erp.entity.School;
import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.DataImportErrorRepository;
import com.school.erp.repository.DataImportJobRepository;
import com.school.erp.repository.OnboardingDraftRepository;
import com.school.erp.repository.SchoolRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.erp.dto.onboarding.OnboardingActivationResponse;
import com.school.erp.dto.onboarding.AdminCredentialsDTO;
import com.school.erp.entity.User;
import com.school.erp.entity.UserRole;
import com.school.erp.entity.UserSchoolRole;
import com.school.erp.repository.UserRepository;
import com.school.erp.repository.UserSchoolRoleRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.security.SecureRandom;

@Service
public class OnboardingDraftService {

    private final OnboardingDraftRepository draftRepository;
    private final SchoolRepository schoolRepository;
    private final DataImportJobRepository jobRepository;
    private final DataImportErrorRepository errorRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final UserSchoolRoleRepository userSchoolRoleRepository;
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    public OnboardingDraftService(OnboardingDraftRepository draftRepository,
                                  SchoolRepository schoolRepository,
                                  DataImportJobRepository jobRepository,
                                  DataImportErrorRepository errorRepository,
                                  ObjectMapper objectMapper,
                                  EntityManager entityManager,
                                  UserRepository userRepository,
                                  UserSchoolRoleRepository userSchoolRoleRepository) {
        this.draftRepository = draftRepository;
        this.schoolRepository = schoolRepository;
        this.jobRepository = jobRepository;
        this.errorRepository = errorRepository;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
        this.userRepository = userRepository;
        this.userSchoolRoleRepository = userSchoolRoleRepository;
    }


    @Transactional
    public OnboardingDraftDTO initDraft(OnboardingInitRequest request) {
        OnboardingDraft draft = new OnboardingDraft();
        draft.setStatus("DRAFT");
        draft.setCurrentStep(1);

        Map<String, Object> step1Map = new HashMap<>();
        step1Map.put("schoolName", request.schoolName() != null ? request.schoolName() : "Draft School");
        step1Map.put("schoolCode", request.schoolCode() != null ? request.schoolCode() : "SCH-" + System.currentTimeMillis() % 10000);
        step1Map.put("boardType", request.boardType() != null ? request.boardType() : "CBSE");
        step1Map.put("principalEmail", request.principalEmail() != null ? request.principalEmail() : "draft@schooly.com");
        step1Map.put("adminPhone", request.adminPhone() != null ? request.adminPhone() : "0000000000");
        if (request.initialMetadata() != null) {
            step1Map.putAll(request.initialMetadata());
        }

        try {
            draft.setStep1Data(objectMapper.writeValueAsString(step1Map));
        } catch (Exception e) {
            throw new BadRequestException("Failed to serialize step 1 data: " + e.getMessage());
        }

        draft = draftRepository.save(draft);

        entityManager.createNativeQuery("INSERT INTO schools (id, name, code, status) VALUES (:id, :name, :code, :status) ON CONFLICT (id) DO NOTHING")
                .setParameter("id", draft.getSchoolId())
                .setParameter("name", request.schoolName() != null ? request.schoolName() : "School " + draft.getSchoolId())
                .setParameter("code", request.schoolCode() != null ? request.schoolCode() : "SCH-" + draft.getSchoolId() + "-" + System.currentTimeMillis() % 1000)
                .setParameter("status", "DRAFT")
                .executeUpdate();

        return toDTO(draft);
    }

    @Transactional(readOnly = true)
    public OnboardingDraftDTO getDraft(Long schoolId) {
        OnboardingDraft draft = draftRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding draft not found with ID: " + schoolId));
        return toDTO(draft);
    }

    @Transactional
    public OnboardingDraftDTO saveStep(OnboardingStepRequest request) {
        OnboardingDraft draft = draftRepository.findById(request.schoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding draft not found with ID: " + request.schoolId()));

        if (request.stepNumber() < 1 || request.stepNumber() > 11) {
            throw new BadRequestException("Invalid step number: " + request.stepNumber() + ". Must be between 1 and 11.");
        }

        draft.setCurrentStep(request.stepNumber());

        try {
            String jsonPayload = objectMapper.writeValueAsString(request.payload());
            switch (request.stepNumber()) {
                case 1 -> draft.setStep1Data(jsonPayload);
                case 2 -> draft.setStep2Data(jsonPayload);
                case 3 -> draft.setStep3Data(jsonPayload);
                case 4 -> draft.setStep4Data(jsonPayload);
                case 5 -> draft.setStep5Data(jsonPayload);
                case 6 -> draft.setStep6Data(jsonPayload);
                case 7 -> draft.setStep7Data(jsonPayload);
                case 8 -> draft.setStep8Data(jsonPayload);
                case 9 -> draft.setStep9Data(jsonPayload);
                case 10 -> draft.setStep10Data(jsonPayload);
                case 11 -> draft.setStep11Data(jsonPayload);
            }
        } catch (Exception e) {
            throw new BadRequestException("Failed to serialize step " + request.stepNumber() + " data: " + e.getMessage());
        }

        draft = draftRepository.save(draft);
        return toDTO(draft);
    }

    @Transactional
    public OnboardingActivationResponse activateSchool(Long schoolId) {
        OnboardingDraft draft = draftRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding draft not found with ID: " + schoolId));

        // Strict Validation Gate: Check for unresolved import errors or failed records
        List<DataImportJob> jobs = jobRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId);
        for (DataImportJob job : jobs) {
            if (job.getFailedRecords() != null && job.getFailedRecords() > 0) {
                throw new BadRequestException("Cannot activate school: Mandatory data import job '" + job.getCategory() + "' has unresolved validation errors (" + job.getFailedRecords() + " failed records). Please resolve all errors in Step 7 before Go-Live.");
            }
            List<DataImportError> unresolvedErrors = errorRepository.findByJobIdAndResolvedFalseOrderByErrorIdAsc(job.getJobId());
            if (!unresolvedErrors.isEmpty()) {
                throw new BadRequestException("Cannot activate school: Mandatory data import job '" + job.getCategory() + "' has " + unresolvedErrors.size() + " unresolved audit errors. Please resolve them before Go-Live.");
            }
        }

        draft.setStatus("ACTIVE");
        draft = draftRepository.save(draft);

        // Provision or update actual School record in schools table
        Map<String, Object> step1 = parseJson(draft.getStep1Data());
        Map<String, Object> step4 = parseJson(draft.getStep4Data());
        String schoolCode = (String) step4.getOrDefault("schoolCode", "SCH-" + schoolId);
        String schoolName = (String) step4.getOrDefault("schoolName", "Onboarded School " + schoolId);

        School school = schoolRepository.findById(schoolId)
                .or(() -> schoolRepository.findByCode(schoolCode))
                .orElseGet(() -> {
                    School s = new School();
                    s.setName(schoolName);
                    s.setCode(schoolCode);
                    return s;
                });
        school.setName(schoolName);
        school.setCode(schoolCode);
        String principalEmail = (String) step4.getOrDefault("principalEmail", step4.getOrDefault("contactEmail", ""));
        String adminPhone = (String) step4.getOrDefault("adminPhone", step4.getOrDefault("contactPhone", ""));
        
        if (principalEmail == null || principalEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Principal Email (or Contact Email) is required for activation.");
        }
        if (adminPhone == null || adminPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin Phone (or Contact Phone) is required for activation.");
        }

        school.setContactEmail(principalEmail);
        school.setContactPhone(adminPhone);
        school.setAddress((String) step4.getOrDefault("address", school.getAddress()));
        school.setSubdomain((String) step4.getOrDefault("subdomain", schoolCode.toLowerCase()));
        school.setStatus("ACTIVE");

        Map<String, Object> meta = school.getMetadata() != null ? new HashMap<>(school.getMetadata()) : new HashMap<>();
        meta.put("subscriptionPlan", step1.getOrDefault("subscriptionPlan", "ENTERPRISE"));
        meta.put("boardType", step4.getOrDefault("boardType", "CBSE"));
        meta.put("udiseCode", step4.getOrDefault("udiseCode", ""));
        
        // Option B: Generate Credentials
        String rawPassword = generateRandomPassword(8);
        meta.put("tempAdminPassword", rawPassword);
        school.setMetadata(meta);

        school = schoolRepository.save(school);
        
        User user = userRepository.findByPhone(adminPhone).orElse(null);
        if (user == null) {
            user = new User();
            user.setPhone(adminPhone);
            user.setName(schoolName + " Admin");
            user.setEmail(principalEmail);
            user.setPasswordHash(PASSWORD_ENCODER.encode(rawPassword));
            user.setStatus("ACTIVE");
            user = userRepository.save(user);
        } else {
            // If user already exists, update password to generated one to give access
            user.setPasswordHash(PASSWORD_ENCODER.encode(rawPassword));
            user = userRepository.save(user);
        }

        UserSchoolRole role = new UserSchoolRole();
        role.setUser(user);
        role.setSchool(school);
        role.setRole(UserRole.ADMIN);
        role.setStatus("ACTIVE");
        userSchoolRoleRepository.save(role);

        OnboardingDraftDTO draftDTO = toDTO(draft);
        AdminCredentialsDTO credentialsDTO = new AdminCredentialsDTO(adminPhone, adminPhone, rawPassword);

        return new OnboardingActivationResponse(draftDTO, credentialsDTO);
    }

    private String generateRandomPassword(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private OnboardingDraftDTO toDTO(OnboardingDraft draft) {
        String updatedAtStr = draft.getUpdatedAt() != null
                ? draft.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : (draft.getCreatedAt() != null ? draft.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");

        return new OnboardingDraftDTO(
                draft.getSchoolId(),
                draft.getStatus(),
                draft.getCurrentStep(),
                parseJson(draft.getStep1Data()),
                parseJson(draft.getStep2Data()),
                parseJson(draft.getStep3Data()),
                parseJson(draft.getStep4Data()),
                parseJson(draft.getStep5Data()),
                parseJson(draft.getStep6Data()),
                parseJson(draft.getStep7Data()),
                parseJson(draft.getStep8Data()),
                parseJson(draft.getStep9Data()),
                parseJson(draft.getStep10Data()),
                parseJson(draft.getStep11Data()),
                updatedAtStr
        );
    }

    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
