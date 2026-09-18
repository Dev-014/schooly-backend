package com.school.erp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmployeeAssetDTO {
    private Long id;
    private Long employeeId;
    private String assetName;
    private String assetType;
    private String serialNumber;
    private LocalDateTime assignedDate;
    private LocalDateTime returnDate;
    private String status;
    private String notes;
}
