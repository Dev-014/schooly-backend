package com.school.erp.service.finance;

import com.school.erp.dto.payment.FeeStatsDTO;
import com.school.erp.dto.payment.StudentFeeRecordDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeeRecordService {
    Page<StudentFeeRecordDTO> getStudentFeeRecords(Long schoolId, Long classId, Long sectionId, String search, String status, Pageable pageable);

    default Page<StudentFeeRecordDTO> getStudentFeeRecords(Long schoolId, Long classId, String search, String status, Pageable pageable) {
        return getStudentFeeRecords(schoolId, classId, null, search, status, pageable);
    }

    FeeStatsDTO getFeeStats(Long schoolId, Long classId, Long sectionId);
    
    default FeeStatsDTO getFeeStats(Long schoolId, Long classId) {
        return getFeeStats(schoolId, classId, null);
    }
}
