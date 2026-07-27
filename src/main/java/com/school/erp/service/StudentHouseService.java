package com.school.erp.service;

import com.school.erp.dto.student.StudentHouseRequest;
import com.school.erp.dto.student.StudentHouseResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.StudentHouse;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentHouseRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentHouseService {

    private final StudentHouseRepository houseRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final AuthContextService authContextService;

    public StudentHouseService(StudentHouseRepository houseRepository, SchoolRepository schoolRepository, StudentRepository studentRepository, AuthContextService authContextService) {
        this.houseRepository = houseRepository;
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.authContextService = authContextService;
    }

    public List<StudentHouseResponse> getAllHouses(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        return houseRepository.findBySchoolId(effectiveSchoolId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StudentHouseResponse createHouse(StudentHouseRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = schoolRepository.findById(effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        StudentHouse house = new StudentHouse();
        house.setSchool(school);
        house.setName(request.name());
        house.setColorCode(request.colorCode());
        house.setDescription(request.description());

        return toResponse(houseRepository.save(house));
    }

    @Transactional
    public StudentHouseResponse updateHouse(Long id, StudentHouseRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        StudentHouse house = houseRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("House not found"));

        house.setName(request.name());
        house.setColorCode(request.colorCode());
        house.setDescription(request.description());

        return toResponse(houseRepository.save(house));
    }

    @Transactional
    public void deleteHouse(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        StudentHouse house = houseRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("House not found"));
        houseRepository.delete(house);
    }

    private StudentHouseResponse toResponse(StudentHouse house) {
        long count = studentRepository.countByHouseId(house.getId());
        return new StudentHouseResponse(
                house.getId(),
                house.getSchool().getId(),
                house.getName(),
                house.getColorCode(),
                house.getDescription(),
                count
        );
    }
}
