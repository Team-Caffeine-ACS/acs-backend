// package com.caffeine.acs_backend.dto.error;
package com.caffeine.acs_backend.enums.errorcode;

public enum ErrorCode {
  // --- Validation ---
  VALIDATION_FAILED,

  // --- Authentication & Authorization ---
  INVALID_CREDENTIALS,
  ACCESS_DENIED,
  INVALID_TOKEN,

  // --- Resource / Business logic ---
  RESOURCE_NOT_FOUND,
  RESOURCE_ALREADY_EXISTS,
  BUSINESS_RULE_VIOLATION,

  // --- Server errors ---
  INTERNAL_ERROR,

  // --- User Related ---
  EMAIL_ALREADY_EXISTS
}
