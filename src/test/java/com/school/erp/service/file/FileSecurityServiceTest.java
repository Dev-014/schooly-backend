package com.school.erp.service.file;

import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.file.FileAttachment;
import com.school.erp.entity.file.FileMetadata;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.student.StudentParent;
import com.school.erp.entity.superadmin.School;
import com.school.erp.enums.file.FileEntityType;
import com.school.erp.enums.file.FileStatus;
import com.school.erp.exception.ForbiddenException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.file.FileAttachmentRepository;
import com.school.erp.repository.student.StudentParentRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.security.AuthenticatedUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileSecurityServiceTest {

    @Mock
    private FileAttachmentRepository fileAttachmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentParentRepository studentParentRepository;

    @InjectMocks
    private FileSecurityService securityService;

    @Test
    @DisplayName("Should throw ResourceNotFoundException for deleted file")
    void testValidateAccess_DeletedFile() {
        FileMetadata deletedFile = FileMetadata.builder()
                .id(UUID.randomUUID())
                .status(FileStatus.DELETED)
                .deletedAt(LocalDateTime.now())
                .build();

        AuthenticatedUser user = new AuthenticatedUser(1L, 1L, UserRole.ADMIN);

        assertThrows(ResourceNotFoundException.class, () -> securityService.validateAccess(deletedFile, user));
    }

    @Test
    @DisplayName("Should block cross-school access with ForbiddenException")
    void testValidateAccess_CrossSchoolAccessBlocked() {
        School schoolA = new School();
        schoolA.setId(1L);

        FileMetadata file = FileMetadata.builder()
                .id(UUID.randomUUID())
                .school(schoolA)
                .status(FileStatus.ACTIVE)
                .build();

        // User from School 2 attempts to access School 1 file
        AuthenticatedUser userFromSchoolB = new AuthenticatedUser(10L, 2L, UserRole.ADMIN);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> securityService.validateAccess(file, userFromSchoolB));
        assertTrue(ex.getMessage().contains("does not belong to user's school"));
    }

    @Test
    @DisplayName("Should allow Super Admin to access file across any school")
    void testValidateAccess_SuperAdminAllowed() {
        School schoolA = new School();
        schoolA.setId(1L);

        FileMetadata file = FileMetadata.builder()
                .id(UUID.randomUUID())
                .school(schoolA)
                .status(FileStatus.ACTIVE)
                .build();

        AuthenticatedUser superAdmin = new AuthenticatedUser(999L, null, UserRole.SUPER_ADMIN);

        when(fileAttachmentRepository.findAllByFileId(file.getId())).thenReturn(List.of());

        assertDoesNotThrow(() -> securityService.validateAccess(file, superAdmin));
    }

    @Test
    @DisplayName("Should block student attempting to access another student's document")
    void testValidateAccess_StudentCrossAccessBlocked() {
        School school = new School();
        school.setId(1L);

        UUID fileId = UUID.randomUUID();
        FileMetadata file = FileMetadata.builder()
                .id(fileId)
                .school(school)
                .status(FileStatus.ACTIVE)
                .build();

        FileAttachment attachment = FileAttachment.builder()
                .entityType(FileEntityType.STUDENT)
                .entityId(200L) // Target student id is 200
                .build();

        when(fileAttachmentRepository.findAllByFileId(fileId)).thenReturn(List.of(attachment));

        // Current student user has student id 100
        Student ownStudent = new Student();
        ownStudent.setId(100L);
        when(studentRepository.findByUserId(50L)).thenReturn(List.of(ownStudent));

        AuthenticatedUser studentUser = new AuthenticatedUser(50L, 1L, UserRole.STUDENT);

        assertThrows(ForbiddenException.class, () -> securityService.validateAccess(file, studentUser));
    }

    @Test
    @DisplayName("Should allow parent to access linked child's report card")
    void testValidateAccess_ParentLinkedChildAllowed() {
        School school = new School();
        school.setId(1L);

        UUID fileId = UUID.randomUUID();
        FileMetadata file = FileMetadata.builder()
                .id(fileId)
                .school(school)
                .status(FileStatus.ACTIVE)
                .build();

        FileAttachment attachment = FileAttachment.builder()
                .entityType(FileEntityType.EXAM_REPORT_CARD)
                .entityId(300L) // Target student id 300
                .build();

        when(fileAttachmentRepository.findAllByFileId(fileId)).thenReturn(List.of(attachment));
        when(studentParentRepository.findByIdParentUserIdAndStudentId(75L, 300L))
                .thenReturn(Optional.of(new StudentParent()));

        AuthenticatedUser parentUser = new AuthenticatedUser(75L, 1L, UserRole.PARENT);

        assertDoesNotThrow(() -> securityService.validateAccess(file, parentUser));
    }

    @Test
    @DisplayName("Should allow parent to upload/access student documents for their linked child")
    void testValidateEntityScope_ParentLinkedStudentAllowed() {
        when(studentParentRepository.findByIdParentUserIdAndStudentId(7L, 5L))
                .thenReturn(Optional.of(new StudentParent()));

        AuthenticatedUser parentUser = new AuthenticatedUser(7L, 1000L, UserRole.PARENT);

        assertDoesNotThrow(() -> securityService.validateEntityScope(
                FileEntityType.STUDENT, 5L, parentUser, 1000L));
    }

    @Test
    @DisplayName("Should block parent uploading/accessing student documents for unlinked child")
    void testValidateEntityScope_ParentUnlinkedStudentBlocked() {
        when(studentParentRepository.findByIdParentUserIdAndStudentId(7L, 999L))
                .thenReturn(Optional.empty());

        AuthenticatedUser parentUser = new AuthenticatedUser(7L, 1000L, UserRole.PARENT);

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> securityService.validateEntityScope(
                FileEntityType.STUDENT, 999L, parentUser, 1000L));
        assertTrue(ex.getMessage().contains("parent is not linked to this student"));
    }
}


