package com.school.erp.service;

import com.school.erp.dto.payment.FeeDueResponse;
import com.school.erp.dto.student.StudentProfileResponse;
import com.school.erp.dto.student.UpdateStudentProfileRequest;
import com.school.erp.entity.academic.Section;
import com.school.erp.entity.attendance.Attendance;
import com.school.erp.entity.student.Student;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.attendance.AttendanceRepository;
import com.school.erp.repository.library.LibraryCirculationRepository;
import com.school.erp.repository.library.LibraryMemberRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.service.finance.FeeDueService;
import com.school.erp.service.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentProfileService {

    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final FeeDueService feeDueService;
    private final StudentService studentService;
    private final AttendanceRepository attendanceRepository;
    private final LibraryCirculationRepository circulationRepository;
    private final LibraryMemberRepository memberRepository;

    public Student resolveStudent(Long studentId, Long schoolId, Long userId) {
        if (studentId != null) {
            if (schoolId != null) {
                return studentRepository.findByIdAndSchoolId(studentId, schoolId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
            } else {
                return studentRepository.findById(studentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
            }
        }
        if (userId != null && schoolId != null) {
            Optional<Student> studentOpt = studentRepository.findByUserId(userId).stream()
                    .filter(s -> s.getSchool() != null && s.getSchool().getId().equals(schoolId))
                    .findFirst();
            if (studentOpt.isPresent()) {
                return studentOpt.get();
            }
        }
        if (schoolId != null) {
            Optional<Student> studentOpt = studentRepository.findBySchoolId(schoolId).stream().findFirst();
            if (studentOpt.isPresent()) {
                return studentOpt.get();
            }
        }
        return studentRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No student records exist in the system"));
    }

    public StudentProfileResponse getProfile(Long studentId, Long schoolId, Long userId) {
        Student student = resolveStudent(studentId, schoolId, userId);
        return toProfileResponse(student);
    }

    @Transactional
    public StudentProfileResponse updateProfile(Long studentId, Long schoolId, Long userId, UpdateStudentProfileRequest request) {
        Student student = resolveStudent(studentId, schoolId, userId);

        if (request.firstName() != null) student.setFirstName(request.firstName().trim());
        if (request.lastName() != null) student.setLastName(request.lastName().trim());
        if (request.firstName() != null || request.lastName() != null) {
            String combined = ((student.getFirstName() != null ? student.getFirstName() : "") + " " +
                               (student.getLastName() != null ? student.getLastName() : "")).trim();
            if (!combined.isBlank()) student.setName(combined);
        }
        if (request.rollNumber() != null) student.setRollNumber(request.rollNumber().trim());
        if (request.phone() != null) student.setPhone(request.phone().trim());
        if (request.email() != null) student.setEmail(request.email().trim());
        if (request.gender() != null) student.setGender(request.gender().trim());
        if (request.dateOfBirth() != null) student.setDateOfBirth(request.dateOfBirth());
        if (request.bloodGroup() != null) student.setBloodGroup(request.bloodGroup().trim());
        if (request.religion() != null) student.setReligion(request.religion().trim());
        if (request.caste() != null) student.setCaste(request.caste().trim());
        if (request.address() != null) student.setAddress(request.address().trim());
        if (request.permanentAddress() != null) student.setPermanentAddress(request.permanentAddress().trim());
        if (request.photoUrl() != null) student.setPhotoUrl(request.photoUrl().trim());

        if (request.guardianName() != null) student.setGuardianName(request.guardianName().trim());
        if (request.guardianRelation() != null) student.setGuardianRelation(request.guardianRelation().trim());
        if (request.guardianPhone() != null) student.setGuardianPhone(request.guardianPhone().trim());
        if (request.guardianEmail() != null) student.setGuardianEmail(request.guardianEmail().trim());
        if (request.guardianOccupation() != null) student.setGuardianOccupation(request.guardianOccupation().trim());

        if (request.fatherName() != null) student.setFatherName(request.fatherName().trim());
        if (request.fatherPhone() != null) student.setFatherPhone(request.fatherPhone().trim());
        if (request.fatherEmail() != null) student.setFatherEmail(request.fatherEmail().trim());
        if (request.fatherOccupation() != null) student.setFatherOccupation(request.fatherOccupation().trim());

        if (request.motherName() != null) student.setMotherName(request.motherName().trim());
        if (request.motherPhone() != null) student.setMotherPhone(request.motherPhone().trim());
        if (request.motherEmail() != null) student.setMotherEmail(request.motherEmail().trim());
        if (request.motherOccupation() != null) student.setMotherOccupation(request.motherOccupation().trim());

        if (request.bankName() != null) student.setBankName(request.bankName().trim());
        if (request.bankAccountNo() != null) student.setBankAccountNo(request.bankAccountNo().trim());
        if (request.bankIfsc() != null) student.setBankIfsc(request.bankIfsc().trim());

        Student saved = studentRepository.save(student);
        if (saved.getSchool() != null && saved.getSchool().getId() != null) {
            try {
                studentService.linkParentAccount(saved, saved.getSchool().getId());
            } catch (Exception ignored) {
            }
        }
        return toProfileResponse(saved);
    }

    public StudentProfileResponse toProfileResponse(Student student) {
        Long schoolId = student.getSchool() != null ? student.getSchool().getId() : null;
        String schoolName = student.getSchool() != null ? student.getSchool().getName() : "Greenwood High";
        Long classId = student.getSchoolClass() != null ? student.getSchoolClass().getId() : null;
        String className = student.getSchoolClass() != null ? student.getSchoolClass().getName() : "Grade 12";

        String sectionName = null;
        if (student.getSectionId() != null && schoolId != null) {
            sectionName = sectionRepository.findByIdAndSchoolId(student.getSectionId(), schoolId)
                    .map(Section::getName)
                    .orElse(null);
        }
        if (sectionName == null) {
            sectionName = "A";
        }

        String categoryName = student.getCategory() != null ? student.getCategory().getName() : "General";
        String houseName = student.getHouse() != null ? student.getHouse().getName() :
                (student.getStudentHouseName() != null ? student.getStudentHouseName() : "Indigo House (Blue)");

        BigDecimal outstandingFees = BigDecimal.ZERO;
        try {
            if (schoolId != null) {
                var dues = feeDueService.getStudentDues(student.getId(), schoolId, null);
                if (dues != null && !dues.isEmpty()) {
                    BigDecimal totalFee = dues.stream().map(FeeDueResponse::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalPaid = dues.stream().map(FeeDueResponse::paidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalDiscount = dues.stream().map(FeeDueResponse::discountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    outstandingFees = totalFee.subtract(totalPaid).subtract(totalDiscount).max(BigDecimal.ZERO);
                }
            }
        } catch (Exception ignored) {
            // Keep default fallback
            outstandingFees = BigDecimal.ZERO;
        }

        BigDecimal attendancePercentage = BigDecimal.ZERO;
        try {
            Long targetSchoolId = schoolId != null ? schoolId : (student.getSchool() != null ? student.getSchool().getId() : null);
            List<Attendance> attendances = targetSchoolId != null
                    ? attendanceRepository.findBySchoolIdAndStudentId(targetSchoolId, student.getId())
                    : attendanceRepository.findByStudentId(student.getId());
            if (attendances != null && !attendances.isEmpty()) {
                long total = attendances.size();
                long present = attendances.stream()
                        .filter(a -> a.getStatus() != null && (
                                "PRESENT".equalsIgnoreCase(a.getStatus()) ||
                                "LATE".equalsIgnoreCase(a.getStatus()) ||
                                "HALF_DAY".equalsIgnoreCase(a.getStatus())
                        ))
                        .count();
                attendancePercentage = BigDecimal.valueOf((present * 100.0) / total)
                        .setScale(1, RoundingMode.HALF_UP);
            }
        } catch (Exception ignored) {
            attendancePercentage = BigDecimal.ZERO;
        }

        Integer booksIssuedCount = 0;
        try {
            Long targetSchoolId = schoolId != null ? schoolId : (student.getSchool() != null ? student.getSchool().getId() : null);
            long count = circulationRepository.countActiveIssuedForStudent(
                    targetSchoolId,
                    student.getId(),
                    student.getAdmissionNo()
            );
            booksIssuedCount = (int) count;
            if (booksIssuedCount == 0 && student.getAdmissionNo() != null) {
                var memberOpt = targetSchoolId != null
                        ? memberRepository.findBySchoolIdAndAdmissionNumber(targetSchoolId, student.getAdmissionNo().trim())
                        : memberRepository.findByAdmissionNumber(student.getAdmissionNo().trim());
                if (memberOpt.isPresent() && memberOpt.get().getActiveIssuedCount() != null) {
                    booksIssuedCount = memberOpt.get().getActiveIssuedCount();
                }
            }
        } catch (Exception ignored) {
            booksIssuedCount = 0;
        }

        String fatherName = student.getFatherName() != null ? student.getFatherName() :
                ("father".equalsIgnoreCase(student.getGuardianRelation()) ? student.getGuardianName() : null);
        String fatherPhone = student.getFatherPhone() != null ? student.getFatherPhone() :
                ("father".equalsIgnoreCase(student.getGuardianRelation()) ? student.getGuardianPhone() : null);
        String fatherEmail = student.getFatherEmail() != null ? student.getFatherEmail() :
                ("father".equalsIgnoreCase(student.getGuardianRelation()) ? student.getGuardianEmail() : null);
        String fatherOccupation = student.getFatherOccupation() != null ? student.getFatherOccupation() :
                ("father".equalsIgnoreCase(student.getGuardianRelation()) ? student.getGuardianOccupation() : null);

        String motherName = student.getMotherName() != null ? student.getMotherName() :
                ("mother".equalsIgnoreCase(student.getGuardianRelation()) ? student.getGuardianName() : null);
        String motherPhone = student.getMotherPhone() != null ? student.getMotherPhone() :
                ("mother".equalsIgnoreCase(student.getGuardianRelation()) ? student.getGuardianPhone() : null);
        String motherEmail = student.getMotherEmail() != null ? student.getMotherEmail() :
                ("mother".equalsIgnoreCase(student.getGuardianRelation()) ? student.getGuardianEmail() : null);
        String motherOccupation = student.getMotherOccupation() != null ? student.getMotherOccupation() :
                ("mother".equalsIgnoreCase(student.getGuardianRelation()) ? student.getGuardianOccupation() : null);

        String bankName = student.getBankName();
        String bankAccountNo = student.getBankAccountNo();
        String bankIfsc = student.getBankIfsc();
        String merit = student.getAcademicMerit();

        String name = student.getName();
        if (name == null || name.isBlank()) {
            name = ((student.getFirstName() != null ? student.getFirstName() : "") + " " +
                    (student.getLastName() != null ? student.getLastName() : "")).trim();
            if (name.isBlank()) name = "Student " + (student.getAdmissionNo() != null ? student.getAdmissionNo() : "");
        }

        return new StudentProfileResponse(
                student.getId(),
                student.getUserId(),
                name,
                student.getAdmissionNo(),
                student.getRollNumber(),
                student.getStatus() != null ? student.getStatus() : "ACTIVE",
                student.getAdmissionDate(),
                schoolId,
                schoolName,
                classId,
                className,
                student.getSectionId(),
                sectionName,
                student.getAcademicYearId(),
                student.getFirstName() != null ? student.getFirstName() : name,
                student.getLastName() != null ? student.getLastName() : "",
                student.getGender() != null ? student.getGender() : "Male",
                student.getDateOfBirth(),
                student.getBloodGroup(),
                student.getReligion(),
                student.getCaste(),
                student.getNationality() != null ? student.getNationality() : "Indian",
                student.getPreviousSchool(),
                student.getAddress(),
                student.getPermanentAddress() != null ? student.getPermanentAddress() : student.getAddress(),
                student.getPhotoUrl(),
                student.getPhone() != null ? student.getPhone() : student.getGuardianPhone(),
                student.getEmail() != null ? student.getEmail() : student.getGuardianEmail(),
                student.getGuardianName(),
                student.getGuardianRelation(),
                student.getGuardianPhone(),
                student.getGuardianEmail(),
                student.getGuardianOccupation(),
                fatherName,
                fatherPhone,
                fatherEmail,
                fatherOccupation,
                motherName,
                motherPhone,
                motherEmail,
                motherOccupation,
                student.getCategory() != null ? student.getCategory().getId() : null,
                categoryName,
                student.getHouse() != null ? student.getHouse().getId() : null,
                houseName,
                student.getFamily() != null ? student.getFamily().getId() : null,
                bankName,
                bankAccountNo,
                bankIfsc,
                merit,
                attendancePercentage,
                outstandingFees,
                booksIssuedCount
        );
    }
}
