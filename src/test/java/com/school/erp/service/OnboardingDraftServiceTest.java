package com.school.erp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.onboarding.OnboardingDraftDTO;
import com.school.erp.entity.DataImportError;
import com.school.erp.entity.DataImportJob;
import com.school.erp.entity.OnboardingDraft;
import com.school.erp.entity.School;
import com.school.erp.exception.BadRequestException;
import com.school.erp.repository.DataImportErrorRepository;
import com.school.erp.repository.DataImportJobRepository;
import com.school.erp.repository.OnboardingDraftRepository;
import com.school.erp.repository.SchoolRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingDraftServiceTest {

    @Mock private OnboardingDraftRepository draftRepository;
    @Mock private SchoolRepository schoolRepository;
    @Mock private DataImportJobRepository jobRepository;
    @Mock private DataImportErrorRepository errorRepository;
    @Mock private EntityManager entityManager;

    private ObjectMapper objectMapper;
    private OnboardingDraftService draftService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        draftService = new OnboardingDraftService(draftRepository, schoolRepository, jobRepository, errorRepository, objectMapper, entityManager);
    }

    @Test
    void activateSchool_whenNoImportErrors_shouldActivateAndStorePlanMetadata() throws Exception {
        OnboardingDraft draft = new OnboardingDraft();
        draft.setSchoolId(101L);
        draft.setStatus("DRAFT");
        draft.setStep1Data(objectMapper.writeValueAsString(new HashMap<String, Object>() {{
            put("schoolName", "Greenwood Academy");
            put("schoolCode", "GA-101");
            put("subscriptionPlan", "PRO");
        }}));

        when(draftRepository.findById(101L)).thenReturn(Optional.of(draft));
        when(draftRepository.save(any(OnboardingDraft.class))).thenAnswer(i -> i.getArgument(0));
        when(jobRepository.findBySchoolIdOrderByCreatedAtDesc(101L)).thenReturn(Collections.emptyList());
        when(schoolRepository.findById(101L)).thenReturn(Optional.empty());
        when(schoolRepository.findByCode("GA-101")).thenReturn(Optional.empty());
        when(schoolRepository.save(any(School.class))).thenAnswer(i -> i.getArgument(0));

        OnboardingDraftDTO result = draftService.activateSchool(101L);

        assertNotNull(result);
        assertEquals("ACTIVE", result.status());
        verify(schoolRepository).save(argThat(school ->
                "Greenwood Academy".equals(school.getName()) &&
                "ACTIVE".equals(school.getStatus()) &&
                "PRO".equals(school.getMetadata().get("subscriptionPlan"))
        ));
    }

    @Test
    void activateSchool_whenFailedRecordsExist_shouldThrowBadRequestException() {
        OnboardingDraft draft = new OnboardingDraft();
        draft.setSchoolId(102L);
        when(draftRepository.findById(102L)).thenReturn(Optional.of(draft));

        DataImportJob job = new DataImportJob();
        job.setJobId(55L);
        job.setCategory("students");
        job.setFailedRecords(3);
        when(jobRepository.findBySchoolIdOrderByCreatedAtDesc(102L)).thenReturn(List.of(job));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> draftService.activateSchool(102L));
        assertTrue(ex.getMessage().contains("Mandatory data import job 'students' has unresolved validation errors (3 failed records)"));
        verify(draftRepository, never()).save(any());
        verify(schoolRepository, never()).save(any());
    }

    @Test
    void activateSchool_whenUnresolvedAuditErrorsExist_shouldThrowBadRequestException() {
        OnboardingDraft draft = new OnboardingDraft();
        draft.setSchoolId(103L);
        when(draftRepository.findById(103L)).thenReturn(Optional.of(draft));

        DataImportJob job = new DataImportJob();
        job.setJobId(56L);
        job.setCategory("teachers");
        job.setFailedRecords(0);
        when(jobRepository.findBySchoolIdOrderByCreatedAtDesc(103L)).thenReturn(List.of(job));

        DataImportError error = new DataImportError();
        error.setErrorId(99L);
        error.setResolved(false);
        when(errorRepository.findByJobIdAndResolvedFalseOrderByErrorIdAsc(56L)).thenReturn(List.of(error));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> draftService.activateSchool(103L));
        assertTrue(ex.getMessage().contains("Mandatory data import job 'teachers' has 1 unresolved audit errors"));
        verify(draftRepository, never()).save(any());
        verify(schoolRepository, never()).save(any());
    }
}
