package com.example.MpApp.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. Handles Quota/Business Rule Violations
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Business Rule Violation", ex.getMessage(), request);
    }

    /**
     * 2. Handles Missing Records (404 Not Found)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    /**
     * 3. Handles Entity/User Creation Duplication (409 Conflict)
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResourceException(
            DuplicateResourceException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.CONFLICT, "Resource Conflict", ex.getMessage(), request);
    }

    /**
     * 4. Handles Bad Authentication Credentials (401 Unauthorized)
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentialsException(
            InvalidCredentialsException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized Access", ex.getMessage(), request);
    }

    /**
     * 5. Handles Expired or Malformed Security JWTs (401 Unauthorized)
     */
    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleJwtAuthenticationException(
            JwtAuthenticationException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid Security Token", ex.getMessage(), request);
    }

    /**
     * 6. Handles Request Body Validation Failures (e.g., @Valid annotations checking @NotBlank, @Min)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String combinedErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return createErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", combinedErrors, request);
    }

    /**
     * 7. Global Catch-All Fallback Handler (Prevents unhandled low-level system leakages)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, HttpServletRequest request) {
        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected system anomaly occurred: " + ex.getMessage(),
                request
        );
    }

    /**
     * Helper utility method to construct standardized error payloads cleanly
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(
            HttpStatus status, String errorType, String message, HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", errorType);
        body.put("message", message);
        body.put("path", request.getRequestURI());

        return new ResponseEntity<>(body, status);
    }
}