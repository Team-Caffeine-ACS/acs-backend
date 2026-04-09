package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.user.UpdateUserRequest;
import com.caffeine.acs_backend.dto.user.UserResponse;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.UserRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserResponse getMe(User currentUser) {
    return UserResponse.from(currentUser);
  }

  @Transactional
  public UserResponse updateMe(User currentUser, UpdateUserRequest request) {
    boolean isChanged = false;

    if (request.email() != null && !request.email().isBlank()) {
      String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);

      if (!normalizedEmail.equals(currentUser.getEmail())) {
        if (userRepository.existsByEmail(normalizedEmail)) {
          throw new BusinessException(
              "Email already in use", ErrorCode.RESOURCE_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }
        currentUser.setEmail(normalizedEmail);
        isChanged = true;
      }
    }

    if (request.password() != null && !request.password().isBlank()) {
      currentUser.setPassword(passwordEncoder.encode(request.password()));
      isChanged = true;
    }

    if (!isChanged) {
      return UserResponse.from(currentUser);
    }

    try {
      return UserResponse.from(userRepository.saveAndFlush(currentUser));
    } catch (DataIntegrityViolationException e) {
      throw new BusinessException(
          "Email already in use", ErrorCode.RESOURCE_ALREADY_EXISTS, HttpStatus.CONFLICT);
    }
  }
}
