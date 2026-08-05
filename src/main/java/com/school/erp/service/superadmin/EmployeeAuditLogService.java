package com.school.erp.service.superadmin;

import com.school.erp.dto.EmployeeAuditLogDTO;
import com.school.erp.entity.superadmin.EmployeeAuditLog;
import com.school.erp.repository.superadmin.EmployeeAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeAuditLogService {

    private final EmployeeAuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<EmployeeAuditLogDTO> getAllAuditLogs() {
        return auditLogRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private EmployeeAuditLogDTO mapToDTO(EmployeeAuditLog log) {
        EmployeeAuditLogDTO dto = new EmployeeAuditLogDTO();
        dto.setId(log.getId());
        if (log.getEmployee() != null) {
            dto.setEmployeeId(log.getEmployee().getId());
            dto.setEmployeeName(log.getEmployee().getUser().getName());
        }
        dto.setAction(log.getAction());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setDetails(log.getDetails());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
