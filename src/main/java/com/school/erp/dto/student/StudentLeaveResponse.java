package com.school.erp.dto.student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record StudentLeaveResponse(
        Long id,
        Long schoolId,
        Long studentId,
        String studentName,
        String admissionNo,
        String classKey,
        LocalDate applyDate,
        LocalDate fromDate,
        LocalDate toDate,
        Integer days,
        String reason,
        String status,
        String reply,
        LocalDateTime createdAt,
        Long classId,
        Long sectionId,
        UUID attachmentFileId,
        String attachmentFileName,
        String attachmentUrl
) {
    public StudentLeaveResponse(
            Long id,
            Long schoolId,
            Long studentId,
            String studentName,
            String admissionNo,
            String classKey,
            LocalDate applyDate,
            LocalDate fromDate,
            LocalDate toDate,
            Integer days,
            String reason,
            String status,
            String reply,
            LocalDateTime createdAt
    ) {
        this(id, schoolId, studentId, studentName, admissionNo, classKey, applyDate, fromDate, toDate, days, reason, status, reply, createdAt, null, null, null, null, null);
    }

    public StudentLeaveResponse(
            Long id,
            Long schoolId,
            Long studentId,
            String studentName,
            String admissionNo,
            String classKey,
            LocalDate applyDate,
            LocalDate fromDate,
            LocalDate toDate,
            Integer days,
            String reason,
            String status,
            String reply,
            LocalDateTime createdAt,
            Long classId,
            Long sectionId
    ) {
        this(id, schoolId, studentId, studentName, admissionNo, classKey, applyDate, fromDate, toDate, days, reason, status, reply, createdAt, classId, sectionId, null, null, null);
    }
}
