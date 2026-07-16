package com.school.erp.service;

import com.school.erp.dto.parent.ParentChildResponse;
import com.school.erp.entity.StudentParent;
import com.school.erp.repository.StudentParentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ParentService {

    private final StudentParentRepository studentParentRepository;

    public ParentService(StudentParentRepository studentParentRepository) {
        this.studentParentRepository = studentParentRepository;
    }

    public List<ParentChildResponse> getChildren(Long userId, Long schoolId) {
        return studentParentRepository.findByIdParentUserIdAndStudentSchoolId(userId, schoolId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ParentChildResponse toResponse(StudentParent studentParent) {
        return new ParentChildResponse(
                studentParent.getStudent().getId(),
                studentParent.getStudent().getName(),
                studentParent.getStudent().getAdmissionNo(),
                studentParent.getStudent().getSchool().getId(),
                studentParent.getStudent().getSchoolClass().getId()
        );
    }
}
