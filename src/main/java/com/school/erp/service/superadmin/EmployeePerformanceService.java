package com.school.erp.service.superadmin;

import com.school.erp.dto.AddPerformanceReviewRequest;
import com.school.erp.dto.EmployeePerformanceDTO;
import com.school.erp.entity.SuperAdminEmployee;
import com.school.erp.entity.superadmin.EmployeePerformance;
import com.school.erp.repository.SuperAdminEmployeeRepository;
import com.school.erp.repository.superadmin.EmployeePerformanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeePerformanceService {

    private final EmployeePerformanceRepository performanceRepository;
    private final SuperAdminEmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<EmployeePerformanceDTO> getAllReviews() {
        return performanceRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeePerformanceDTO addReview(AddPerformanceReviewRequest request) {
        SuperAdminEmployee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + request.getEmployeeId()));

        SuperAdminEmployee reviewer = null;
        if (request.getReviewerId() != null) {
            reviewer = employeeRepository.findById(request.getReviewerId())
                    .orElseThrow(() -> new RuntimeException("Reviewer not found with id: " + request.getReviewerId()));
        }

        EmployeePerformance performance = new EmployeePerformance();
        performance.setEmployee(employee);
        performance.setReviewCycle(request.getReviewCycle());
        performance.setRating(request.getRating());
        performance.setReviewer(reviewer);
        performance.setComments(request.getComments());
        performance.setGoals(request.getGoals());

        EmployeePerformance savedPerformance = performanceRepository.save(performance);
        return mapToDTO(savedPerformance);
    }

    private EmployeePerformanceDTO mapToDTO(EmployeePerformance performance) {
        EmployeePerformanceDTO dto = new EmployeePerformanceDTO();
        dto.setId(performance.getId());
        dto.setEmployeeId(performance.getEmployee().getId());
        dto.setEmployeeName(performance.getEmployee().getUser().getName());
        dto.setReviewCycle(performance.getReviewCycle());
        dto.setRating(performance.getRating());
        
        if (performance.getReviewer() != null) {
            dto.setReviewerId(performance.getReviewer().getId());
            dto.setReviewerName(performance.getReviewer().getUser().getName());
        }
        
        dto.setComments(performance.getComments());
        dto.setGoals(performance.getGoals());
        return dto;
    }
}
