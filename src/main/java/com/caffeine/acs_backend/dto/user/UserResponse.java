package com.caffeine.acs_backend.dto.user;

import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.enums.UserRole;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        UserRole role,
        UUID personId
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getPerson() != null ? user.getPerson().getId() : null
        );
    }
}
