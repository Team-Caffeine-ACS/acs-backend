package com.caffeine.acs_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @Schema(description = "User email address", example = "user@example.com")
        @NotBlank
        @Email(message = "E-posti formaat on vale")
        String email,
    @Schema(description = "User password", example = "StrongPassword123!") @NotBlank String password

    // @NotBlank @Email String email, @NotBlank String password
    ) {}
