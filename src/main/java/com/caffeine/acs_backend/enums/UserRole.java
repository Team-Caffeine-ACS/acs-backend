package com.caffeine.acs_backend.enums;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Role assigned to the user")
public enum UserRole {
  VISITOR,
  RECEPTIONIST,
  SECURITY_CHIEF,
  ADMIN
}
