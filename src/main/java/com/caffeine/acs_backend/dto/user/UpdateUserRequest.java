package com.caffeine.acs_backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

@Schema(description = "Payload for updating the authenticated user's profile")
public record UpdateUserRequest(    
        @Schema(description = "User email address", example = "user@example.com")
        @Email(message = "E-posti formaat on vale")
        String email,

        @Schema(description = "User password", example = "StrongPassword123!")
        String password    
) {}
