package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.parent.ParentChildResponse;
import com.school.erp.security.AuthContextService;
import com.school.erp.service.ParentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/parent")
public class ParentController {

    private final ParentService parentService;
    private final AuthContextService authContextService;

    public ParentController(ParentService parentService, AuthContextService authContextService) {
        this.parentService = parentService;
        this.authContextService = authContextService;
    }

    @GetMapping("/children")
    public ResponseEntity<ApiResponse<List<ParentChildResponse>>> getChildren() {
        var authenticatedUser = authContextService.requireCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(
                parentService.getChildren(authenticatedUser.userId(), authenticatedUser.schoolId()),
                "Children fetched successfully"
        ));
    }
}
