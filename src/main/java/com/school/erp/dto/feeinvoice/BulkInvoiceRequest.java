package com.school.erp.dto.feeinvoice;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BulkInvoiceRequest {
    @NotNull(message = "Fee structure ID is required")
    private Long feeStructureId;
    
    @NotNull(message = "Class ID is required")
    private Long classId;
    
    private Long schoolId;
    
    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
}
