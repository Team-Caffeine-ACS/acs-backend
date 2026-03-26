package com.caffeine.acs_backend.dto.user;

import jakarta.validation.constraints.Email;

public record UpdateUserRequest(@Email String email, String password) {}
