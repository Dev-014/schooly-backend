package com.school.erp.service;

import com.school.erp.entity.AuditLog;
import com.school.erp.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for managing audit logs for super admin actions.
 * Logs all platform-level operations performed by super admins.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log a super admin action.
     *
     * @param actorId           The ID of the super admin performing the action
     * @param actorName         The name of the super admin
     * @param action            The action being performed (e.g., SCHOOL_CREATED, MODULE_ENABLED)
     * @param resourceType      The type of resource being acted upon
     * @param resourceId        The ID of the resource
     * @param targetSchoolId    The school ID being affected (optional)
     * @param targetSchoolName  The school name (optional)
     * @param changesJson       JSON representation of changes made
     * @param ipAddress         IP address of the request
     * @param userAgent         User agent string
     * @return The saved AuditLog entity
     */
    public AuditLog logAction(Long actorId, String actorName, String action, String resourceType,
                             Long resourceId, Long targetSchoolId, String targetSchoolName,
                             String changesJson, String ipAddress, String userAgent) {

        AuditLog auditLog = new AuditLog();
        auditLog.setActorId(actorId);
        auditLog.setActorName(actorName);
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setTargetSchoolId(targetSchoolId);
        auditLog.setTargetSchoolName(targetSchoolName);
        auditLog.setChangesJson(changesJson);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLog.setStatus("SUCCESS");

        AuditLog saved = auditLogRepository.save(auditLog);
        log.info("Audit log created: action={}, actor={}, targetSchool={}, timestamp={}",
                action, actorName, targetSchoolId, saved.getTimestamp());

        return saved;
    }

    /**
     * Log a failed action.
     */
    public AuditLog logFailedAction(Long actorId, String actorName, String action,
                                   String resourceType, Long resourceId, Long targetSchoolId,
                                   String targetSchoolName, String errorReason,
                                   String ipAddress, String userAgent) {

        AuditLog auditLog = new AuditLog();
        auditLog.setActorId(actorId);
        auditLog.setActorName(actorName);
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setTargetSchoolId(targetSchoolId);
        auditLog.setTargetSchoolName(targetSchoolName);
        auditLog.setChangesJson(errorReason);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLog.setStatus("FAILED");

        AuditLog saved = auditLogRepository.save(auditLog);
        log.warn("Audit log created for failed action: action={}, actor={}, error={}",
                action, actorName, errorReason);

        return saved;
    }

    /**
     * Retrieve audit logs with filtering.
     *
     * @param schoolId School ID filter (optional)
     * @param action   Action type filter (optional)
     * @param actorId  Actor ID filter (optional)
     * @param startDate Start date filter (optional)
     * @param endDate   End date filter (optional)
     * @param pageable  Pagination info
     * @return Page of matching AuditLog entries
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Long schoolId, String action, Long actorId,
                                      LocalDateTime startDate, LocalDateTime endDate,
                                      Pageable pageable) {
        return auditLogRepository.findByFilters(schoolId, action, actorId, startDate, endDate, pageable);
    }

    /**
     * Get audit logs for a specific school.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getSchoolAuditLogs(Long schoolId, Pageable pageable) {
        return auditLogRepository.findByTargetSchoolId(schoolId, pageable);
    }

    /**
     * Get audit logs for a specific action.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getActionAuditLogs(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable);
    }
}
