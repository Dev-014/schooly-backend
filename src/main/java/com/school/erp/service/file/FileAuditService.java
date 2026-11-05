package com.school.erp.service.file;

import com.school.erp.entity.auth.User;
import com.school.erp.entity.file.FileAuditLog;
import com.school.erp.entity.file.FileMetadata;
import com.school.erp.entity.superadmin.School;
import com.school.erp.enums.file.FileAuditAction;
import com.school.erp.repository.file.FileAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileAuditService {

    private final FileAuditLogRepository fileAuditLogRepository;

    @Transactional
    public void recordEvent(
            School school,
            FileMetadata file,
            User user,
            FileAuditAction action,
            String ipAddress,
            String detailsJson
    ) {
        FileAuditLog logEntry = FileAuditLog.builder()
                .school(school)
                .file(file)
                .user(user)
                .action(action)
                .ipAddress(ipAddress)
                .details(detailsJson)
                .build();
        fileAuditLogRepository.save(logEntry);
    }
}
