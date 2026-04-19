package com.caffeine.acs_backend.dto.auth;

import com.caffeine.acs_backend.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @Schema(description = "User email address", example = "user@example.com")
        @NotBlank
        @Email(message = "E-posti formaat on vale")
        String email,
    @Schema(description = "User password", example = "StrongPassword123!") @NotBlank @ValidPassword
        String password) {}
