package com.school.erp.service.exam;

import com.school.erp.dto.exam.AdmitCardPreviewResponse;
import com.school.erp.dto.exam.AdmitCardRosterItemResponse;
import com.school.erp.dto.exam.AdmitCardStatsResponse;
import com.school.erp.dto.exam.GenerateAdmitCardRequest;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.academic.Section;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.exam.ExamAdmitCard;
import com.school.erp.entity.exam.ExamApplicability;
import com.school.erp.entity.exam.ExamSchedule;
import com.school.erp.entity.exam.ExamSetup;
import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.exam.ExamAdmitCardRepository;
import com.school.erp.repository.exam.ExamApplicabilityRepository;
import com.school.erp.repository.exam.ExamScheduleRepository;
import com.school.erp.repository.exam.ExamSetupRepository;
import com.school.erp.security.AuthContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamAdmitCardService {

    private final AuthContextService authContextService;
    private final ExamAdmitCardRepository examAdmitCardRepository;
    private final ExamApplicabilityRepository examApplicabilityRepository;
    private final ExamSetupRepository examSetupRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final SectionRepository sectionRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    @Transactional
    public Page<AdmitCardRosterItemResponse> filterAdmitCards(
            Long rawSchoolId,
            Long examSetupId,
            Long classId,
            Long sectionId,
            String status,
            Pageable pageable) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        // Ensure student entries are synced in admit card table
        if (examSetupId != null) {
            if (classId != null) {
                syncRosterForClass(schoolId, examSetupId, classId, sectionId);
            } else {
                syncRosterForSchool(schoolId, examSetupId);
            }
        }

        Page<ExamAdmitCard> page = examAdmitCardRepository.filterAdmitCards(
                schoolId, examSetupId, classId, sectionId, status, pageable);

        return page.map(this::mapToRosterItem);
    }

    @Transactional(readOnly = true)
    public Long resolveDefaultExamSetupId(Long rawSchoolId, Student student) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        List<ExamSetup> setups = examSetupRepository.findBySchoolIdOrderByOrderNoAsc(schoolId);
        if (setups.isEmpty()) {
            throw new ResourceNotFoundException("No examinations have been configured yet");
        }
        if (student.getSchoolClass() != null) {
            Long classId = student.getSchoolClass().getId();
            for (ExamSetup setup : setups) {
                if (!examScheduleRepository
                        .findBySchoolIdAndExamSetupIdAndSchoolClassIdOrderByExamDateAscStartTimeAsc(
                                schoolId, setup.getId(), classId).isEmpty()) {
                    return setup.getId();
                }
            }
        }
        return setups.get(0).getId();
    }

    @Transactional(readOnly = true)
    public AdmitCardPreviewResponse getPreview(Long rawSchoolId, Long examSetupId, Long studentId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        ExamSetup setup = examSetupRepository.findByIdAndSchoolId(examSetupId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam setup not found with id: " + examSetupId));

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        Optional<ExamAdmitCard> admitCardOpt = examAdmitCardRepository
                .findBySchoolIdAndExamSetupIdAndStudentId(schoolId, examSetupId, studentId);

        // Retrieve schedules for student's class
        List<ExamSchedule> schedules = examScheduleRepository
                .findBySchoolIdAndExamSetupIdAndSchoolClassIdOrderByExamDateAscStartTimeAsc(
                        schoolId, examSetupId, student.getSchoolClass().getId());

        List<AdmitCardPreviewResponse.AdmitCardScheduleItem> scheduleItems = schedules.stream()
                .map(s -> AdmitCardPreviewResponse.AdmitCardScheduleItem.builder()
                        .subjectName(s.getSubject().getName())
                        .subjectCode(s.getSubject().getCode())
                        .date(s.getExamDate())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .formattedTime(s.getStartTime().format(TIME_FORMATTER))
                        .roomNumber(s.getRoomNumber())
                        .build())
                .collect(Collectors.toList());

        String sectionName = "";
        if (student.getSectionId() != null) {
            sectionName = sectionRepository.findById(student.getSectionId())
                    .map(Section::getName)
                    .orElse("");
        }

        String gradeAndSec = student.getSchoolClass().getName();
        if (!sectionName.isBlank()) {
            gradeAndSec += " - " + sectionName;
        }

        return AdmitCardPreviewResponse.builder()
                .schoolName(school.getName() != null ? school.getName().toUpperCase() : "SCHOLAR SLATE INTERNATIONAL")
                .schoolCode(school.getCode())
                .examName(setup.getName() != null ? setup.getName().toUpperCase() : "ANNUAL EXAMINATION 2024")
                .termName(setup.getTerm() != null ? setup.getTerm().getName() : "")
                .candidateName(student.getName() != null ? student.getName().toUpperCase() : "")
                .rollNumber(student.getRollNumber() != null ? student.getRollNumber() : String.valueOf(student.getId()))
                .admissionNo(student.getAdmissionNo())
                .gradeAndSection(gradeAndSec)
                .verificationStatus("VERIFIED")
                .status(admitCardOpt.map(ExamAdmitCard::getStatus).orElse("PENDING"))
                .generatedAt(admitCardOpt.map(ExamAdmitCard::getGeneratedAt).orElse(null))
                .releasedAt(admitCardOpt.map(ExamAdmitCard::getReleasedAt).orElse(null))
                .cardNumber(admitCardOpt.map(ExamAdmitCard::getCardNumber).orElse("AC-" + student.getRollNumber()))
                .templateName(admitCardOpt.map(ExamAdmitCard::getTemplateName).orElse("STANDARD"))
                .schedule(scheduleItems)
                .build();
    }

    @Transactional
    public List<AdmitCardRosterItemResponse> generateAdmitCards(Long rawSchoolId, GenerateAdmitCardRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        ExamSetup setup = examSetupRepository.findByIdAndSchoolId(request.getExamSetupId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam setup not found with id: " + request.getExamSetupId()));

        List<Student> studentsToProcess;
        if (request.getStudentIds() != null && !request.getStudentIds().isEmpty()) {
            studentsToProcess = studentRepository.findAllById(request.getStudentIds()).stream()
                    .filter(s -> s.getSchool().getId().equals(schoolId))
                    .collect(Collectors.toList());
        } else if (request.getClassId() != null) {
            studentsToProcess = (request.getSectionId() != null)
                    ? studentRepository.findBySchoolIdAndSchoolClassIdAndSectionId(schoolId, request.getClassId(), request.getSectionId())
                    : studentRepository.findBySchoolIdAndSchoolClassId(schoolId, request.getClassId());
        } else {
            throw new BadRequestException("Either studentIds or classId must be provided for admit card generation");
        }

        List<ExamApplicability> applicabilities = examApplicabilityRepository.findBySchoolIdAndExamSetupId(schoolId, setup.getId());
        if (!applicabilities.isEmpty()) {
            studentsToProcess = studentsToProcess.stream()
                    .filter(s -> s.getSchoolClass() != null && applicabilities.stream().anyMatch(a ->
                            a.getSchoolClass().getId().equals(s.getSchoolClass().getId()) &&
                            (a.getSection() == null || s.getSectionId() == null || a.getSection().getId().equals(s.getSectionId()))))
                    .collect(Collectors.toList());
        }

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        List<ExamAdmitCard> savedCards = studentsToProcess.stream().map(student -> {
            ExamAdmitCard card = examAdmitCardRepository
                    .findBySchoolIdAndExamSetupIdAndStudentId(schoolId, setup.getId(), student.getId())
                    .orElseGet(() -> {
                        ExamAdmitCard c = new ExamAdmitCard();
                        c.setSchool(school);
                        c.setExamSetup(setup);
                        c.setStudent(student);
                        c.setSchoolClass(student.getSchoolClass());
                        if (student.getSectionId() != null) {
                            c.setSection(sectionRepository.findById(student.getSectionId()).orElse(null));
                        }
                        c.setRollNumber(student.getRollNumber());
                        return c;
                    });

            card.setStatus("GENERATED");
            card.setGeneratedAt(LocalDateTime.now());
            if (card.getCardNumber() == null) {
                String identifier = (student.getAdmissionNo() != null && !student.getAdmissionNo().isBlank())
                        ? student.getAdmissionNo()
                        : String.valueOf(student.getId());
                card.setCardNumber("AC-" + schoolId + "-" + setup.getId() + "-" + identifier);
            }
            if (request.getTemplateName() != null) {
                card.setTemplateName(request.getTemplateName());
            }
            return examAdmitCardRepository.save(card);
        }).collect(Collectors.toList());

        return savedCards.stream().map(this::mapToRosterItem).collect(Collectors.toList());
    }

    @Transactional
    public int releaseAllCards(Long rawSchoolId, Long examSetupId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        ExamSetup setup = examSetupRepository.findByIdAndSchoolId(examSetupId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam setup not found with id: " + examSetupId));

        return examAdmitCardRepository.releaseAllGeneratedCards(schoolId, setup.getId(), LocalDateTime.now());
    }

    @Transactional
    public AdmitCardStatsResponse getStats(Long rawSchoolId, Long examSetupId) {
        return getStats(rawSchoolId, examSetupId, null);
    }

    @Transactional
    public AdmitCardStatsResponse getStats(Long rawSchoolId, Long examSetupId, Long classId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        if (examSetupId != null) {
            if (classId != null) {
                syncRosterForClass(schoolId, examSetupId, classId, null);
            } else {
                syncRosterForSchool(schoolId, examSetupId);
            }
        }

        long total;
        long generated;
        long pending;
        long released;

        if (classId != null) {
            total = examAdmitCardRepository.countBySchoolIdAndExamSetupIdAndSchoolClassId(schoolId, examSetupId, classId);
            generated = examAdmitCardRepository.countBySchoolIdAndExamSetupIdAndSchoolClassIdAndStatus(schoolId, examSetupId, classId, "GENERATED");
            pending = examAdmitCardRepository.countBySchoolIdAndExamSetupIdAndSchoolClassIdAndStatus(schoolId, examSetupId, classId, "PENDING");
            released = examAdmitCardRepository.countBySchoolIdAndExamSetupIdAndSchoolClassIdAndStatus(schoolId, examSetupId, classId, "RELEASED");
        } else {
            total = examAdmitCardRepository.countBySchoolIdAndExamSetupId(schoolId, examSetupId);
            generated = examAdmitCardRepository.countBySchoolIdAndExamSetupIdAndStatus(schoolId, examSetupId, "GENERATED");
            pending = examAdmitCardRepository.countBySchoolIdAndExamSetupIdAndStatus(schoolId, examSetupId, "PENDING");
            released = examAdmitCardRepository.countBySchoolIdAndExamSetupIdAndStatus(schoolId, examSetupId, "RELEASED");
        }

        return AdmitCardStatsResponse.builder()
                .totalStudents(total)
                .generatedCount(generated)
                .pendingCount(pending)
                .releasedCount(released)
                .build();
    }

    private void syncRosterForSchool(Long schoolId, Long examSetupId) {
        ExamSetup setup = examSetupRepository.findByIdAndSchoolId(examSetupId, schoolId).orElse(null);
        if (setup == null) return;

        List<ExamApplicability> applicabilities = examApplicabilityRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId);
        List<Student> students;

        if (!applicabilities.isEmpty()) {
            students = new java.util.ArrayList<>();
            for (ExamApplicability app : applicabilities) {
                Long cId = app.getSchoolClass().getId();
                if (app.getSection() != null) {
                    students.addAll(studentRepository.findBySchoolIdAndSchoolClassIdAndSectionId(schoolId, cId, app.getSection().getId()));
                } else {
                    students.addAll(studentRepository.findBySchoolIdAndSchoolClassId(schoolId, cId));
                }
            }
            students = students.stream().distinct().collect(Collectors.toList());

            // Purge admit card records for students whose class/section is no longer applicable
            List<ExamAdmitCard> existingCards = examAdmitCardRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId);
            for (ExamAdmitCard card : existingCards) {
                boolean isStillApplicable = applicabilities.stream().anyMatch(app ->
                        app.getSchoolClass().getId().equals(card.getSchoolClass().getId()) &&
                        (app.getSection() == null || card.getSection() == null || app.getSection().getId().equals(card.getSection().getId())));
                if (!isStillApplicable) {
                    examAdmitCardRepository.delete(card);
                }
            }
        } else {
            students = studentRepository.findBySchoolId(schoolId);
        }

        syncStudentsToRoster(setup, students);
    }

    private void syncRosterForClass(Long schoolId, Long examSetupId, Long classId, Long sectionId) {
        ExamSetup setup = examSetupRepository.findByIdAndSchoolId(examSetupId, schoolId).orElse(null);
        if (setup == null) return;

        List<ExamApplicability> applicabilities = examApplicabilityRepository.findBySchoolIdAndExamSetupId(schoolId, examSetupId);
        if (!applicabilities.isEmpty()) {
            boolean isApplicable = applicabilities.stream().anyMatch(app ->
                    app.getSchoolClass().getId().equals(classId) &&
                    (app.getSection() == null || sectionId == null || app.getSection().getId().equals(sectionId)));
            if (!isApplicable) {
                List<ExamAdmitCard> cardsToDelete = examAdmitCardRepository.findBySchoolIdAndExamSetupIdAndSchoolClassId(schoolId, examSetupId, classId);
                if (!cardsToDelete.isEmpty()) {
                    examAdmitCardRepository.deleteAll(cardsToDelete);
                }
                return;
            }
        }

        List<Student> students = (sectionId != null)
                ? studentRepository.findBySchoolIdAndSchoolClassIdAndSectionId(schoolId, classId, sectionId)
                : studentRepository.findBySchoolIdAndSchoolClassId(schoolId, classId);
        syncStudentsToRoster(setup, students);
    }

    private void syncStudentsToRoster(ExamSetup setup, List<Student> students) {
        Long schoolId = setup.getSchool().getId();
        School school = setup.getSchool();
        for (Student student : students) {
            if (student.getSchoolClass() == null) {
                continue;
            }
            Optional<ExamAdmitCard> existing = examAdmitCardRepository
                    .findBySchoolIdAndExamSetupIdAndStudentId(schoolId, setup.getId(), student.getId());
            if (existing.isEmpty()) {
                ExamAdmitCard card = new ExamAdmitCard();
                card.setSchool(school);
                card.setExamSetup(setup);
                card.setStudent(student);
                card.setSchoolClass(student.getSchoolClass());
                if (student.getSectionId() != null) {
                    card.setSection(sectionRepository.findById(student.getSectionId()).orElse(null));
                }
                card.setRollNumber(student.getRollNumber());
                String identifier = (student.getAdmissionNo() != null && !student.getAdmissionNo().isBlank())
                        ? student.getAdmissionNo()
                        : String.valueOf(student.getId());
                card.setCardNumber("AC-" + schoolId + "-" + setup.getId() + "-" + identifier);
                card.setStatus("PENDING");
                examAdmitCardRepository.save(card);
            }
        }
    }

    private AdmitCardRosterItemResponse mapToRosterItem(ExamAdmitCard card) {
        String sectionName = "";
        if (card.getSection() != null) {
            sectionName = card.getSection().getName();
        } else if (card.getStudent().getSectionId() != null) {
            sectionName = sectionRepository.findById(card.getStudent().getSectionId())
                    .map(Section::getName)
                    .orElse("");
        }

        return AdmitCardRosterItemResponse.builder()
                .id(card.getId())
                .studentId(card.getStudent().getId())
                .studentName(card.getStudent().getName())
                .rollNumber(card.getRollNumber() != null ? card.getRollNumber() : card.getStudent().getRollNumber())
                .admissionNo(card.getStudent().getAdmissionNo())
                .classId(card.getSchoolClass().getId())
                .className(card.getSchoolClass().getName())
                .sectionId(card.getSection() != null ? card.getSection().getId() : card.getStudent().getSectionId())
                .sectionName(sectionName)
                .status(card.getStatus())
                .cardNumber(card.getCardNumber())
                .generatedAt(card.getGeneratedAt())
                .releasedAt(card.getReleasedAt())
                .build();
    }
}
