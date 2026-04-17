package com.caffeine.acs_backend.dto.user;

import com.caffeine.acs_backend.enums.UserRole;
import com.caffeine.acs_backend.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for admin creating a new user")
public record AdminCreateUserRequest(
    @Schema(description = "User email address", example = "john.doe@example.com")
        @NotBlank
        @Email(message = "E-posti formaat on vale")
        String email,
    @Schema(description = "User password", example = "StrongPassword1!") @NotBlank @ValidPassword
        String password,
    @Schema(description = "Role assigned to the user, defaults to VISITOR", example = "VISITOR")
        UserRole role) {}
