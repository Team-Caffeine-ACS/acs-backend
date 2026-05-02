package com.caffeine.acs_backend.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.caffeine.acs_backend.dto.error.ErrorResponse;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;
  private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
    request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/test");
  }

  @Test
  void handleUnknownUser_returns401() {
    var ex = new UsernameNotFoundException("User not found");
    ResponseEntity<ErrorResponse> response = handler.handleUnknownUser(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
  }

  @Test
  void handleBadCredentials_returns401() {
    var ex = new BadCredentialsException("Bad credentials");
    ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody().getMessage()).isEqualTo("Invalid credentials");
  }

  @Test
  void handleBusinessException_returnsCustomStatus() {
    var ex =
        new BusinessException(
            "Business error", ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.I_AM_A_TEAPOT);
    ResponseEntity<ErrorResponse> response = handler.handleBusiness(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
    assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.BUSINESS_RULE_VIOLATION);
  }

  @Test
  void handleValidationException_returns400WithDetails() {
    // ValidationExceptioni mockimine on "trikiga"
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("object", "email", "must be valid");
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

    // Me ei saa MethodArgumentNotValidExceptionit lihtsalt uuega luua, kasutame kavalust
    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().getDetails()).containsKey("email");
    assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED);
  }

  @Test
  void handleDuplicateEmail_returns409() {
    Exception ex = new Exception("Constraint violation");
    ResponseEntity<ErrorResponse> response = handler.handleDuplicateEmail(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.RESOURCE_ALREADY_EXISTS);
  }

  @Test
  void handleUnexpected_returns500() {
    Exception ex = new RuntimeException("Boom");
    ResponseEntity<ErrorResponse> response = handler.handleUnexpected(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody().getMessage()).isEqualTo("Unexpected internal server error");
  }

  @Test
  void handleAccessDenied_returns403() {
    var ex = new org.springframework.security.access.AccessDeniedException("No access");
    ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
  }
}
