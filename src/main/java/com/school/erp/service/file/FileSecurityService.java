package com.school.erp.service.file;

import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.file.FileAttachment;
import com.school.erp.entity.file.FileMetadata;
import com.school.erp.entity.student.Student;
import com.school.erp.enums.file.FileEntityType;
import com.school.erp.enums.file.FileStatus;
import com.school.erp.exception.ForbiddenException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.file.FileAttachmentRepository;
import com.school.erp.repository.student.StudentParentRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileSecurityService {

    private final FileAttachmentRepository fileAttachmentRepository;
    private final StudentRepository studentRepository;
    private final StudentParentRepository studentParentRepository;

    public void validateAccess(FileMetadata file, AuthenticatedUser currentUser) {
        if (file.getDeletedAt() != null || file.getStatus() == FileStatus.DELETED) {
            throw new ResourceNotFoundException("File not found or has been deleted");
        }

        // 1. Tenant boundary validation
        if (currentUser.role() != UserRole.SUPER_ADMIN) {
            if (currentUser.schoolId() == null || !file.getSchool().getId().equals(currentUser.schoolId())) {
                log.warn("Cross-school access blocked: user schoolId={}, file schoolId={}", currentUser.schoolId(), file.getSchool().getId());
                throw new ForbiddenException("Access denied: file does not belong to user's school");
            }
        }

        // 2. Entity relationship validation
        List<FileAttachment> attachments = fileAttachmentRepository.findAllByFileId(file.getId());
        for (FileAttachment attachment : attachments) {
            validateEntityScope(attachment.getEntityType(), attachment.getEntityId(), currentUser, file.getSchool().getId());
        }
    }

    public void validateEntityScope(FileEntityType entityType, Long entityId, AuthenticatedUser currentUser, Long schoolId) {
        if (currentUser.role() == UserRole.SUPER_ADMIN || currentUser.role() == UserRole.ADMIN || currentUser.role() == UserRole.STAFF || currentUser.role() == UserRole.TEACHER) {
            return; // Admins, staff, and teachers have tenant-wide or classroom authorization
        }

        if (entityType == FileEntityType.STUDENT) {
            if (currentUser.role() == UserRole.STUDENT) {
                List<Student> students = studentRepository.findByUserId(currentUser.userId());
                boolean isOwnStudent = students.stream().anyMatch(s -> s.getId().equals(entityId));
                if (!isOwnStudent) {
                    throw new ForbiddenException("Access denied: student cannot access another student's documents");
                }
            } else if (currentUser.role() == UserRole.PARENT) {
                boolean isLinked = studentParentRepository.findByIdParentUserIdAndStudentId(currentUser.userId(), entityId).isPresent();
                if (!isLinked) {
                    throw new ForbiddenException("Access denied: parent is not linked to this student");
                }
            }
        } else if (entityType == FileEntityType.EXAM_REPORT_CARD) {
            if (currentUser.role() == UserRole.STUDENT) {
                List<Student> students = studentRepository.findByUserId(currentUser.userId());
                boolean isOwnReport = students.stream().anyMatch(s -> s.getId().equals(entityId));
                if (!isOwnReport) {
                    throw new ForbiddenException("Access denied: cannot view another student's report card");
                }
            } else if (currentUser.role() == UserRole.PARENT) {
                boolean isLinked = studentParentRepository.findByIdParentUserIdAndStudentId(currentUser.userId(), entityId).isPresent();
                if (!isLinked) {
                    throw new ForbiddenException("Access denied: report card does not belong to a linked child");
                }
            }
        }
    }
}

