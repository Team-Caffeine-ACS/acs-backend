package com.caffeine.acs_backend.dto.user;

import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Response object representing a user profile")
public record UserResponse(
    @Schema(
            description = "Unique identifier of the user",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
    @Schema(description = "User email address", example = "john@example.com") String email,
    @Schema(description = "Role assigned to the user", example = "ADMIN") UserRole role,
    @Schema(
            description = "Identifier of the related person entity",
            example = "550e8400-e29b-41d4-a716-446655440111")
        UUID personId) {
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getRole(),
        user.getPerson() != null ? user.getPerson().getId() : null);
  }
}
