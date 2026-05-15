package com.booknest.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex,
                                                               HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(RuntimeException ex,
                                                              HttpServletRequest request) {
        String message = ex.getMessage();
        if (message != null && message.contains("Authentication failed")) {
            message = "Payment gateway authentication failed. Use valid Test API keys from "
                    + "https://dashboard.razorpay.com/app/keys (same key pair for RAZORPAY_KEY_ID and "
                    + "RAZORPAY_KEY_SECRET), then restart payment-service.";
            return build(HttpStatus.BAD_GATEWAY, message, request.getRequestURI());
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR, message, request.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message == null || message.isBlank() ? "Unexpected error" : message);
        body.put("path", path);
        return ResponseEntity.status(status).body(body);
    }
}