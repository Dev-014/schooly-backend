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

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OnboardingDraftService {

    private final OnboardingDraftRepository draftRepository;
    private final SchoolRepository schoolRepository;
    private final DataImportJobRepository jobRepository;
    private final DataImportErrorRepository errorRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    public OnboardingDraftService(OnboardingDraftRepository draftRepository,
                                  SchoolRepository schoolRepository,
                                  DataImportJobRepository jobRepository,
                                  DataImportErrorRepository errorRepository,
                                  ObjectMapper objectMapper,
                                  EntityManager entityManager) {
        this.draftRepository = draftRepository;
        this.schoolRepository = schoolRepository;
        this.jobRepository = jobRepository;
        this.errorRepository = errorRepository;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }


    @Transactional
    public OnboardingDraftDTO initDraft(OnboardingInitRequest request) {
        OnboardingDraft draft = new OnboardingDraft();
        draft.setStatus("DRAFT");
        draft.setCurrentStep(1);

        Map<String, Object> step1Map = new HashMap<>();
        step1Map.put("schoolName", request.schoolName());
        step1Map.put("schoolCode", request.schoolCode() != null ? request.schoolCode() : "SCH-" + System.currentTimeMillis() % 10000);
        step1Map.put("boardType", request.boardType() != null ? request.boardType() : "CBSE");
        step1Map.put("principalEmail", request.principalEmail());
        step1Map.put("adminPhone", request.adminPhone());
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

        if (request.stepNumber() < 1 || request.stepNumber() > 9) {
            throw new BadRequestException("Invalid step number: " + request.stepNumber() + ". Must be between 1 and 9.");
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
            }
        } catch (Exception e) {
            throw new BadRequestException("Failed to serialize step " + request.stepNumber() + " data: " + e.getMessage());
        }

        draft = draftRepository.save(draft);
        return toDTO(draft);
    }

    @Transactional
    public OnboardingDraftDTO activateSchool(Long schoolId) {
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
        String schoolCode = (String) step1.getOrDefault("schoolCode", "SCH-" + schoolId);
        String schoolName = (String) step1.getOrDefault("schoolName", "Onboarded School " + schoolId);

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
        school.setContactEmail((String) step1.getOrDefault("principalEmail", step1.getOrDefault("contactEmail", "admin@school.com")));
        school.setContactPhone((String) step1.getOrDefault("adminPhone", step1.getOrDefault("contactPhone", "9999999999")));
        school.setAddress((String) step1.getOrDefault("address", school.getAddress()));
        school.setSubdomain((String) step1.getOrDefault("subdomain", schoolCode.toLowerCase()));
        school.setStatus("ACTIVE");

        Map<String, Object> meta = school.getMetadata() != null ? new HashMap<>(school.getMetadata()) : new HashMap<>();
        meta.put("subscriptionPlan", step1.getOrDefault("subscriptionPlan", "ENTERPRISE"));
        meta.put("boardType", step1.getOrDefault("boardType", "CBSE"));
        meta.put("udiseCode", step1.getOrDefault("udiseCode", ""));
        school.setMetadata(meta);

        schoolRepository.save(school);

        return toDTO(draft);
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
