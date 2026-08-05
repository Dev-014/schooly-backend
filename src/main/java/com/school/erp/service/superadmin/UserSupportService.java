package com.school.erp.service.superadmin;

import com.school.erp.dto.superadmin.PasswordResetDTO;
import com.school.erp.dto.superadmin.UserRequestDTO;
import com.school.erp.entity.PasswordReset;
import com.school.erp.entity.User;
import com.school.erp.entity.UserRequest;
import com.school.erp.repository.PasswordResetRepository;
import com.school.erp.repository.UserRepository;
import com.school.erp.repository.UserRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSupportService {

    private final PasswordResetRepository passwordResetRepository;
    private final UserRequestRepository userRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    public PasswordResetDTO sendPasswordResetLink(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        PasswordReset reset = PasswordReset.builder()
                .user(user)
                .build();
        
        passwordResetRepository.save(reset);
        
        // Mock sending email
        System.out.println("Sending reset link to " + user.getEmail() + ": /reset-password?token=" + reset.getToken());
        
        return mapToPasswordResetDTO(reset);
    }
    
    @Transactional(readOnly = true)
    public List<PasswordResetDTO> getAllPasswordResets() {
        return passwordResetRepository.findAll().stream()
                .map(this::mapToPasswordResetDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserRequestDTO> getAllUserRequests(String status) {
        List<UserRequest> requests = status != null && !status.isEmpty() 
                ? userRequestRepository.findByStatus(status) 
                : userRequestRepository.findAll();
                
        return requests.stream()
                .map(this::mapToUserRequestDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserRequestDTO approveUserRequest(Long requestId) {
        UserRequest request = userRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        request.setStatus("APPROVED");
        userRequestRepository.save(request);
        
        // Additional logic like actually elevating the role would go here
        
        return mapToUserRequestDTO(request);
    }

    @Transactional
    public UserRequestDTO rejectUserRequest(Long requestId, String notes) {
        UserRequest request = userRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        request.setStatus("REJECTED");
        if (notes != null && !notes.isEmpty()) {
            request.setNotes(notes);
        }
        userRequestRepository.save(request);
        
        return mapToUserRequestDTO(request);
    }

    private PasswordResetDTO mapToPasswordResetDTO(PasswordReset reset) {
        return PasswordResetDTO.builder()
                .id(reset.getId())
                .userId(reset.getUser().getId())
                .userName(reset.getUser().getName() != null ? reset.getUser().getName() : "Unknown User")
                .userEmail(reset.getUser().getEmail())
                .token(reset.getToken() != null ? reset.getToken().toString() : null)
                .status(reset.getStatus())
                .expiresAt(reset.getExpiresAt())
                .createdAt(reset.getCreatedAt())
                .build();
    }

    private UserRequestDTO mapToUserRequestDTO(UserRequest request) {
        return UserRequestDTO.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .userName(request.getUser().getName() != null ? request.getUser().getName() : "Unknown User")
                .userEmail(request.getUser().getEmail())
                .schoolId(request.getSchool() != null ? request.getSchool().getId() : null)
                .schoolName(request.getSchool() != null ? request.getSchool().getName() : null)
                .requestType(request.getRequestType())
                .status(request.getStatus())
                .notes(request.getNotes())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
