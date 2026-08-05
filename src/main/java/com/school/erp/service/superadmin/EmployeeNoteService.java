package com.school.erp.service.superadmin;

import com.school.erp.dto.EmployeeNoteDTO;
import com.school.erp.entity.superadmin.EmployeeNote;
import com.school.erp.repository.superadmin.EmployeeNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeNoteService {

    private final EmployeeNoteRepository noteRepository;

    @Transactional(readOnly = true)
    public List<EmployeeNoteDTO> getNotesByEmployeeId(Long employeeId) {
        return noteRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private EmployeeNoteDTO mapToDTO(EmployeeNote note) {
        EmployeeNoteDTO dto = new EmployeeNoteDTO();
        dto.setId(note.getId());
        if (note.getEmployee() != null) {
            dto.setEmployeeId(note.getEmployee().getId());
        }
        dto.setNoteContent(note.getNoteContent());
        if (note.getAuthor() != null) {
            dto.setAuthorId(note.getAuthor().getId());
            dto.setAuthorName(note.getAuthor().getUser().getName());
        }
        dto.setCreatedAt(note.getCreatedAt());
        return dto;
    }
}
