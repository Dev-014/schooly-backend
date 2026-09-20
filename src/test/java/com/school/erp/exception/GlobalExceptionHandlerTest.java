package com.school.erp.exception;

import com.school.erp.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleMethodArgumentTypeMismatch_shouldReturnBadRequestWithClearMessage() {
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "undefined",
                Long.class,
                "schoolId",
                null,
                new NumberFormatException("For input string: \"undefined\"")
        );

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/students");

        ResponseEntity<ApiResponse<Void>> response = handler.handleMethodArgumentTypeMismatch(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("schoolId"));
        assertTrue(response.getBody().message().contains("undefined"));
    }

    @Test
    void handleHttpMessageNotReadable_shouldReturnBadRequest() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Malformed JSON", (org.springframework.http.HttpInputMessage) null);

        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/students");

        ResponseEntity<ApiResponse<Void>> response = handler.handleHttpMessageNotReadable(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Malformed or unreadable request body", response.getBody().message());
    }

    @Test
    void handleGeneric_shouldReturnInternalServerError() {
        RuntimeException exception = new RuntimeException("Something failed");

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/test");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneric(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Something failed", response.getBody().message());
    }
}
