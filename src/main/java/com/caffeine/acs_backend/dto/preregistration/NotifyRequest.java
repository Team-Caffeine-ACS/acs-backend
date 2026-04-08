package com.caffeine.acs_backend.dto.preregistration;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to resend notification email")
public record NotifyRequest(@Schema(description = "Optional custom message") String message) {}
