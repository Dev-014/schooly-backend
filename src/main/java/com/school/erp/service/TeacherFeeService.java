package com.school.erp.service;

import com.school.erp.dto.payment.FeeReminderResponse;
import com.school.erp.dto.payment.SendFeeReminderRequest;
import com.school.erp.dto.payment.TeacherClassResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeacherFeeService {
    List<TeacherClassResponse> getAssignedClasses(Long schoolId);
    void sendReminders(Long schoolId, SendFeeReminderRequest request);
    Page<FeeReminderResponse> getReminderHistory(Long schoolId, Pageable pageable);
}
