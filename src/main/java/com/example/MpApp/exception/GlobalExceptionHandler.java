package com.example.MpApp.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .collect(Collectors.joining(", "));
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", message, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "Forbidden",
                ex.getMessage() != null ? ex.getMessage() : "Access denied", request);
    }

    /**
     * 7a. OTP failures are expected user errors (wrong code, expired code,
     * cooldown). They must not reach the 500 catch-all below, which would both
     * misreport them as server faults and echo the internal message.
     */
    @ExceptionHandler(OtpException.class)
    public ResponseEntity<Map<String, Object>> handleOtpException(
            OtpException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), request);
    }

    /**
     * 7b. Throttling. Advertises how long to wait so a well-behaved client does
     * not have to guess.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitExceededException(
            RateLimitExceededException ex, HttpServletRequest request) {
        ResponseEntity<Map<String, Object>> response =
                createErrorResponse(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", ex.getMessage(), request);
        response.getHeaders().add("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
        return response;
    }

    /**
     * 8a. A request for a URL that does not map to anything must answer 404, not
     * fall through to the catch-all below.
     *
     * <p>Spring raises this for an unmapped path. Because it was unhandled it
     * reached the {@code Exception} handler and came back as 500 "An unexpected
     * error occurred", so every typo in a path or a client pointing at a route
     * that does not exist looked like a server fault. That is how a whole
     * forgot-password API drifting out of sync with the deployed backend went
     * unnoticed: the live probe reported a server error instead of "no such
     * endpoint".
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "Not Found",
                "The requested endpoint does not exist.", request);
    }

    /** Same reason as above: a wrong verb is a client error, not a server fault. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed",
                ex.getMessage(), request);
    }

    /** A malformed or unsupported JSON body is likewise the caller's problem. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableBody(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Malformed Request",
                "The request body could not be read. Check that it is valid JSON.", request);
    }

    /** DTO validation that happens on method parameters rather than the body. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Required parameter '" + ex.getParameterName() + "' is missing.", request);
    }

    /**
     * 8b. Global Catch-All Fallback Handler (Prevents unhandled low-level system leakages)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, HttpServletRequest request) {

        // Deliberately does not include ex.getMessage(). The previous version
        // appended it, which leaked SQL fragments, file paths and internal class
        // names to any caller who could trigger an unexpected error. The real
        // cause still goes to the server log; the client gets a generic message
        // plus the correlation-free request path.
        LOGGER.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);

        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request
        );
    }

    /**
     * 9. Handles authenticated users attempting to access another staff member's data.
     */
    @ExceptionHandler(OwnershipViolationException.class)
    public ResponseEntity<Map<String, Object>> handleOwnershipViolationException(
            OwnershipViolationException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), request);
    }

    /**
     * Helper utility method to construct standardized error payloads cleanly
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(
            HttpStatus status, String errorType, String message, HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("errorCode", errorType.toUpperCase().replace(" ", "_"));
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", errorType);
        body.put("message", message != null ? message : "Request failed");
        body.put("path", request.getRequestURI());

        return new ResponseEntity<>(body, status);
    }
}