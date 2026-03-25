package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.user.UpdateUserRequest;
import com.caffeine.acs_backend.dto.user.UserResponse;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserResponse getMe(User currentUser) {
    return UserResponse.from(currentUser);
  }

  public UserResponse updateMe(User currentUser, UpdateUserRequest request) {
    if (request.email() != null && !request.email().isBlank()) {
      if (!request.email().equals(currentUser.getEmail())
          && userRepository.existsByEmail(request.email())) {
        throw new IllegalArgumentException("Email already in use");
      }
      currentUser.setEmail(request.email());
    }

    if (request.password() != null && !request.password().isBlank()) {
      currentUser.setPassword(passwordEncoder.encode(request.password()));
    }

    return UserResponse.from(userRepository.save(currentUser));
  }
}
