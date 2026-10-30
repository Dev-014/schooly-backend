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
    private Long sectionId;
    private String photoUrl;
    
    private BigDecimal totalFees;
    private BigDecimal amountPaid;
    private BigDecimal balance;
    
    private LocalDate lastPaymentDate;
    private String status;

    public StudentFeeRecordDTO(String id, String studentId, String studentName, String admissionNumber,
                               String classGroupName, String photoUrl, BigDecimal totalFees, BigDecimal amountPaid,
                               BigDecimal balance, LocalDate lastPaymentDate, String status) {
        this(id, studentId, studentName, admissionNumber, classGroupName, null, photoUrl, totalFees, amountPaid, balance, lastPaymentDate, status);
    }
}
