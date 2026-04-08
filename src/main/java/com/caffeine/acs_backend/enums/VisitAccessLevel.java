package com.caffeine.acs_backend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Access Level for guests")
public enum VisitAccessLevel {
  STANDARD_GUEST,
  RESTRICTED_AREA
}