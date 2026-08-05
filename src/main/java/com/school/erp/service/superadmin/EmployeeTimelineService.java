package com.school.erp.service.superadmin;

import com.school.erp.dto.EmployeeTimelineDTO;
import com.school.erp.entity.superadmin.EmployeeTimeline;
import com.school.erp.repository.superadmin.EmployeeTimelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeTimelineService {

    private final EmployeeTimelineRepository timelineRepository;

    @Transactional(readOnly = true)
    public List<EmployeeTimelineDTO> getTimelineByEmployeeId(Long employeeId) {
        return timelineRepository.findByEmployeeIdOrderByDateDesc(employeeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private EmployeeTimelineDTO mapToDTO(EmployeeTimeline timeline) {
        EmployeeTimelineDTO dto = new EmployeeTimelineDTO();
        dto.setId(timeline.getId());
        if (timeline.getEmployee() != null) {
            dto.setEmployeeId(timeline.getEmployee().getId());
        }
        dto.setTitle(timeline.getTitle());
        dto.setDescription(timeline.getDescription());
        dto.setDate(timeline.getDate());
        return dto;
    }
}
