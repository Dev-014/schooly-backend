package com.school.erp.service.finance;

import com.school.erp.dto.finance.SchoolAccountRequest;
import com.school.erp.dto.finance.SchoolAccountResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.SchoolAccount;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolAccountRepository;
import com.school.erp.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolAccountService {

    private final SchoolAccountRepository schoolAccountRepository;
    private final SchoolRepository schoolRepository;

    @Transactional
    public SchoolAccountResponse createAccount(Long schoolId, SchoolAccountRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        SchoolAccount account = new SchoolAccount();
        account.setSchool(school);
        account.setAccountName(request.getAccountName());
        account.setAccountType(request.getAccountType());
        account.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        account = schoolAccountRepository.save(account);
        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public List<SchoolAccountResponse> getAccounts(Long schoolId) {
        return schoolAccountRepository.findBySchoolId(schoolId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SchoolAccountResponse toggleActiveStatus(Long id, Long schoolId) {
        SchoolAccount account = schoolAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getSchool().getId().equals(schoolId)) {
            throw new IllegalArgumentException("Account does not belong to the school");
        }

        account.setIsActive(!account.getIsActive());
        account = schoolAccountRepository.save(account);
        return mapToResponse(account);
    }

    private SchoolAccountResponse mapToResponse(SchoolAccount account) {
        SchoolAccountResponse response = new SchoolAccountResponse();
        response.setId(account.getId());
        response.setAccountName(account.getAccountName());
        response.setAccountType(account.getAccountType());
        response.setIsActive(account.getIsActive());
        return response;
    }
}
