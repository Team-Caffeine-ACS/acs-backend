package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.auth.AuthResponse;
import com.caffeine.acs_backend.dto.auth.LoginRequest;
import com.caffeine.acs_backend.dto.auth.RegisterRequest;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.enums.UserRole;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.UserRepository;
import com.caffeine.acs_backend.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      // throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
      throw new BusinessException(
          "Email already in use", ErrorCode.RESOURCE_ALREADY_EXISTS, HttpStatus.CONFLICT);
    }

    User user =
        User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .role(UserRole.VISITOR)
            .build();

    userRepository.save(user);

    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    return new AuthResponse(accessToken, refreshToken);
  }

  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password()));

    User user = userRepository.findByEmail(request.email()).orElseThrow();

    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    return new AuthResponse(accessToken, refreshToken);
  }

  public AuthResponse refresh(String refreshToken) {
    Claims claims = jwtService.extractClaims(refreshToken);

    String type = claims.get("type", String.class);
    if (!"refresh".equals(type)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token type");
    }

    String email = claims.getSubject();

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    if (!jwtService.isTokenValid(refreshToken, user)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
    }

    String newAccessToken = jwtService.generateAccessToken(user);

    return new AuthResponse(newAccessToken, refreshToken);
  }
}
