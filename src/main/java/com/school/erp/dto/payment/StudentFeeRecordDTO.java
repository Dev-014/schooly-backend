package com.school.erp.dto.payment;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentFeeRecordDTO {
    private String id;
    private String studentId;
    private String studentName;
    private String admissionNumber;
    private String classGroupName;
    private String photoUrl;
    
    private BigDecimal totalFees;
    private BigDecimal amountPaid;
    private BigDecimal balance;
    
    private LocalDate lastPaymentDate;
    private String status;
}
