package com.school.erp.controller.superadmin;

import com.school.erp.dto.EmployeeNoteDTO;
import com.school.erp.service.superadmin.EmployeeNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/super-admin/employee-notes")
@RequiredArgsConstructor
public class EmployeeNoteController {

    private final EmployeeNoteService noteService;

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeeNoteDTO>> getNotesByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(noteService.getNotesByEmployeeId(employeeId));
    }
}
