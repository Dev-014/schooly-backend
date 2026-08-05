package com.school.erp.service.superadmin;

import com.school.erp.dto.EmployeeAssetDTO;
import com.school.erp.entity.superadmin.EmployeeAsset;
import com.school.erp.repository.superadmin.EmployeeAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeAssetService {

    private final EmployeeAssetRepository assetRepository;

    @Transactional(readOnly = true)
    public List<EmployeeAssetDTO> getAssetsByEmployeeId(Long employeeId) {
        return assetRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private EmployeeAssetDTO mapToDTO(EmployeeAsset asset) {
        EmployeeAssetDTO dto = new EmployeeAssetDTO();
        dto.setId(asset.getId());
        if (asset.getEmployee() != null) {
            dto.setEmployeeId(asset.getEmployee().getId());
        }
        dto.setAssetName(asset.getAssetName());
        dto.setAssetType(asset.getAssetType());
        dto.setSerialNumber(asset.getSerialNumber());
        dto.setAssignedDate(asset.getAssignedDate());
        dto.setReturnDate(asset.getReturnDate());
        dto.setStatus(asset.getStatus());
        dto.setNotes(asset.getNotes());
        return dto;
    }
}
