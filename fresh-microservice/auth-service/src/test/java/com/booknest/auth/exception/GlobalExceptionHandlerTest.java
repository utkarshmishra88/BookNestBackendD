package com.booknest.auth.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleAccessDenied() {
        org.springframework.security.access.AccessDeniedException ex = new org.springframework.security.access.AccessDeniedException("Denied");
        ResponseEntity<Map<String, Object>> resp = handler.handleAccessDenied(ex);
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        assertEquals("Denied", resp.getBody().get("message"));
    }

    @Test
    void testHandleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult br = mock(org.springframework.validation.BindingResult.class);
        when(ex.getBindingResult()).thenReturn(br);
        when(br.getFieldErrors()).thenReturn(java.util.List.of(new FieldError("obj", "field", "msg")));
        
        ResponseEntity<Map<String, Object>> resp = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody().get("fieldErrors"));
    }

    @Test
    void testHandleRuntimeException() {
        RuntimeException ex = new RuntimeException("Runtime error");
        ResponseEntity<Map<String, Object>> resp = handler.handleAny(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
    }

    @Test
    void testHandleStatus() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Test msg");
        ResponseEntity<Map<String, Object>> resp = handler.handleStatus(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Test msg", resp.getBody().get("error"));
    }
}
