package com.school.erp.dto.finance;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class FeeInstallmentResponse {
    private Long id;
    private Long academicYearId;
    private String name;
    private LocalDate startDate;
    private LocalDate dueDate;
    private List<FeeInstallmentItemResponse> items;
}
