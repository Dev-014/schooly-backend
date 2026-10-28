package com.school.erp.dto.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAssignmentRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesWithAcademicYearIdAlias() throws Exception {
        String json = "{\"assignmentType\":\"class_teacher\",\"academicYearId\":3,\"classId\":5,\"sectionId\":8}";
        UserAssignmentRequest request = objectMapper.readValue(json, UserAssignmentRequest.class);

        assertNotNull(request);
        assertEquals("class_teacher", request.assignmentType());
        assertEquals(3L, request.academicSessionId());
        assertEquals(5L, request.classId());
        assertEquals(8L, request.sectionId());
    }

    @Test
    void deserializesWithAcademicSessionId() throws Exception {
        String json = "{\"assignmentType\":\"class_teacher\",\"academicSessionId\":3,\"classId\":5,\"sectionId\":8}";
        UserAssignmentRequest request = objectMapper.readValue(json, UserAssignmentRequest.class);

        assertNotNull(request);
        assertEquals("class_teacher", request.assignmentType());
        assertEquals(3L, request.academicSessionId());
        assertEquals(5L, request.classId());
        assertEquals(8L, request.sectionId());
    }
}
