package com.caffeine.acs_backend.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response")
public record ErrorResponse(
    @Schema(description = "Error message", example = "Invalid credentials") String message,
    @Schema(description = "HTTP status code", example = "401") int status) {}
