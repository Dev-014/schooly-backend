package com.school.erp.service.finance;

import com.school.erp.dto.payment.FeeStatsDTO;
import com.school.erp.dto.payment.StudentFeeRecordDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeeRecordService {
    Page<StudentFeeRecordDTO> getStudentFeeRecords(Long schoolId, Long classId, String search, String status, Pageable pageable);
    FeeStatsDTO getFeeStats(Long schoolId);
}
