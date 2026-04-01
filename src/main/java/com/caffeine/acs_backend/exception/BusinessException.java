package com.caffeine.acs_backend.exception;

import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus status;

    public BusinessException(String message, ErrorCode errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public BusinessException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.status = status;
    }
}
