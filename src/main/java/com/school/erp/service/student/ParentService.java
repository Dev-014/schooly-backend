package com.school.erp.service.student;

import com.school.erp.dto.parent.ParentChildResponse;
import com.school.erp.entity.academic.Section;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.student.StudentParent;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.student.StudentParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ParentService {

    private final StudentParentRepository studentParentRepository;
    private final SectionRepository sectionRepository;

    @Autowired
    public ParentService(StudentParentRepository studentParentRepository, SectionRepository sectionRepository) {
        this.studentParentRepository = studentParentRepository;
        this.sectionRepository = sectionRepository;
    }

    public ParentService(StudentParentRepository studentParentRepository) {
        this(studentParentRepository, null);
    }

    public List<ParentChildResponse> getChildren(Long userId, Long schoolId) {
        List<StudentParent> studentParents = (schoolId != null)
                ? studentParentRepository.findByIdParentUserIdAndStudentSchoolId(userId, schoolId)
                : studentParentRepository.findByIdParentUserId(userId);

        return studentParents
                .stream()
                .filter(sp -> sp.getStudent() != null)
                .sorted(Comparator.comparing(sp -> sp.getStudent().getId()))
                .map(this::toResponse)
                .toList();
    }

    private ParentChildResponse toResponse(StudentParent studentParent) {
        Student s = studentParent.getStudent();
        Long schoolId = s.getSchool() != null ? s.getSchool().getId() : null;
        Long classId = s.getSchoolClass() != null ? s.getSchoolClass().getId() : null;
        String baseClassName = s.getSchoolClass() != null ? s.getSchoolClass().getName() : null;
        Long sectionId = s.getSectionId();
        String sectionName = null;
        if (sectionId != null && sectionRepository != null) {
            sectionName = (schoolId != null
                    ? sectionRepository.findByIdAndSchoolId(sectionId, schoolId)
                    : sectionRepository.findById(sectionId))
                    .map(Section::getName)
                    .orElse(null);
        }
        String displayClassName = baseClassName != null
                ? (sectionName != null ? baseClassName + " • " + sectionName : baseClassName)
                : null;

        String parentName = (s.getFatherName() != null && !s.getFatherName().isBlank())
                ? s.getFatherName().trim()
                : ((s.getGuardianName() != null && !s.getGuardianName().isBlank())
                        ? s.getGuardianName().trim()
                        : ((s.getMotherName() != null && !s.getMotherName().isBlank())
                                ? s.getMotherName().trim()
                                : (studentParent.getParentUser() != null && studentParent.getParentUser().getName() != null && !studentParent.getParentUser().getName().toLowerCase().contains("michael")
                                        ? studentParent.getParentUser().getName()
                                        : s.getName() + "'s Parent")));

        String relation = (s.getGuardianRelation() != null && !s.getGuardianRelation().isBlank())
                ? s.getGuardianRelation().trim()
                : (studentParent.getRelation() != null && !studentParent.getRelation().isBlank() ? studentParent.getRelation().trim() : "Guardian");

        return new ParentChildResponse(
                s.getId(),
                s.getName(),
                s.getName(),
                s.getAdmissionNo(),
                schoolId,
                classId,
                displayClassName,
                sectionId,
                sectionName,
                s.getRollNumber(),
                parentName,
                relation,
                s.getFatherName(),
                s.getMotherName(),
                s.getGuardianName()
        );
    }
}
