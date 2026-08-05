package com.school.erp.service.superadmin;

import com.school.erp.dto.EmployeeLifecycleDTO;
import com.school.erp.entity.superadmin.EmployeeLifecycle;
import com.school.erp.repository.superadmin.EmployeeLifecycleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeLifecycleService {

    private final EmployeeLifecycleRepository lifecycleRepository;

    @Transactional(readOnly = true)
    public List<EmployeeLifecycleDTO> getLifecycleEventsByEmployeeId(Long employeeId) {
        return lifecycleRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private EmployeeLifecycleDTO mapToDTO(EmployeeLifecycle event) {
        EmployeeLifecycleDTO dto = new EmployeeLifecycleDTO();
        dto.setId(event.getId());
        if (event.getEmployee() != null) {
            dto.setEmployeeId(event.getEmployee().getId());
        }
        dto.setEventType(event.getEventType());
        dto.setEventDate(event.getEventDate());
        dto.setDescription(event.getDescription());
        if (event.getCreatedBy() != null) {
            dto.setCreatedBy(event.getCreatedBy().getId());
            dto.setCreatedByName(event.getCreatedBy().getUser().getName());
        }
        return dto;
    }
}
