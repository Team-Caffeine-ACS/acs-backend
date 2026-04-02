package com.caffeine.acs_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT token refresh")
public record RefreshRequest(
    @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken) {}
