package com.school.erp.service.file;

import com.school.erp.entity.auth.User;
import com.school.erp.entity.file.FileAuditLog;
import com.school.erp.entity.file.FileMetadata;
import com.school.erp.entity.superadmin.School;
import com.school.erp.enums.file.FileAuditAction;
import com.school.erp.repository.file.FileAuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileAuditServiceTest {

    @Mock
    private FileAuditLogRepository fileAuditLogRepository;

    @InjectMocks
    private FileAuditService fileAuditService;

    @Test
    @DisplayName("Should successfully record file audit log event within transaction")
    void testRecordEvent_Success() {
        School school = new School();
        school.setId(1L);

        FileMetadata file = FileMetadata.builder()
                .id(UUID.randomUUID())
                .school(school)
                .originalFileName("doc.pdf")
                .build();

        User user = new User();
        user.setId(10L);

        fileAuditService.recordEvent(school, file, user, FileAuditAction.UPLOAD_INITIATED, "192.168.1.1", "{\"fileName\":\"doc.pdf\"}");

        ArgumentCaptor<FileAuditLog> captor = ArgumentCaptor.forClass(FileAuditLog.class);
        verify(fileAuditLogRepository).save(captor.capture());

        FileAuditLog saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(school, saved.getSchool());
        assertEquals(file, saved.getFile());
        assertEquals(user, saved.getUser());
        assertEquals(FileAuditAction.UPLOAD_INITIATED, saved.getAction());
        assertEquals("192.168.1.1", saved.getIpAddress());
    }
}

