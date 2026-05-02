package com.caffeine.acs_backend.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class BusinessExceptionTest {

  @Test
  void constructor_withAllFields_setsFieldsCorrectly() {
    // GIVEN
    String message = "Custom validation failed message";
    ErrorCode code = ErrorCode.VALIDATION_FAILED;
    HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;

    // ACT
    BusinessException ex = new BusinessException(message, code, status);

    // ASSERT
    assertThat(ex.getMessage()).isEqualTo(message);
    assertThat(ex.getErrorCode()).isEqualTo(code);
    assertThat(ex.getStatus()).isEqualTo(status);
  }

  @Test
  void constructor_withErrorCodeOnly_setsMessageAsEnumName() {
    // GIVEN
    ErrorCode code = ErrorCode.INTERNAL_ERROR;
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    // ACT
    BusinessException ex = new BusinessException(code, status);

    // ASSERT
    // See testib rida: super(errorCode.name())
    assertThat(ex.getMessage()).isEqualTo("INTERNAL_ERROR");
    assertThat(ex.getErrorCode()).isEqualTo(code);
    assertThat(ex.getStatus()).isEqualTo(status);
  }

  @Test
  void businessException_withResourceNotFound_worksCorrectly() {
    // GIVEN
    ErrorCode code = ErrorCode.RESOURCE_NOT_FOUND;
    HttpStatus status = HttpStatus.NOT_FOUND;

    // ACT
    BusinessException ex = new BusinessException("User not found", code, status);

    // ASSERT
    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
