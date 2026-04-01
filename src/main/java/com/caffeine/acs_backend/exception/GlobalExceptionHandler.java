package com.caffeine.acs_backend.exception;

import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.dto.error.ErrorResponse;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 401: Unknown user ---
    @ExceptionHandler(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUnknownUser(
            org.springframework.security.core.userdetails.UsernameNotFoundException ex,
            HttpServletRequest request
    ) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .message("Invalid credentials")
                .status(HttpStatus.UNAUTHORIZED.value())
                .errorCode(ErrorCode.INVALID_CREDENTIALS)
                .path(request.getRequestURI())
                .details(null)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // --- 401: Wrong password ---
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex,
            HttpServletRequest request
    ) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .message("Invalid credentials")
                .status(HttpStatus.UNAUTHORIZED.value())
                .errorCode(ErrorCode.INVALID_CREDENTIALS)
                .path(request.getRequestURI())
                .details(null)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // --- 400: Validation errors ---
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String field = ex.getBindingResult().getFieldErrors().get(0).getField();
        String message = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .message(message)
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode(ErrorCode.VALIDATION_FAILED)
                .path(request.getRequestURI())
                .details(Map.of(field, message))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // --- BusinessException ---
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex,
            HttpServletRequest request
    ) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .errorCode(ex.getErrorCode())
                .path(request.getRequestURI())
                .details(null)
                .build();

        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler({
        org.springframework.dao.DataIntegrityViolationException.class,
        org.hibernate.exception.ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            Exception ex,
            HttpServletRequest request
    ) {

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .message("Resource already exists")
                .status(HttpStatus.CONFLICT.value())
                .errorCode(ErrorCode.RESOURCE_ALREADY_EXISTS)
                .path(request.getRequestURI())
                .details(null)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // --- 500: Fallback (MUST BE LAST) ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .message("Unexpected internal server error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode(ErrorCode.INTERNAL_ERROR)
                .path(request.getRequestURI())
                .details(null)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}