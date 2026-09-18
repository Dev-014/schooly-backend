package com.school.erp.service;

import com.school.erp.dto.TicketCategoryDTO;
import com.school.erp.entity.TicketCategory;
import com.school.erp.repository.TicketCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketCategoryService {

    private final TicketCategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<TicketCategoryDTO> getAllActiveCategories() {
        return categoryRepository.findByStatus("ACTIVE").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TicketCategoryDTO mapToDTO(TicketCategory entity) {
        return TicketCategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .department(entity.getDepartment())
                .status(entity.getStatus())
                .build();
    }
}
