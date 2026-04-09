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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
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
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.email(), request.password()));

    } catch (AuthenticationException e) {
      throw new BusinessException(
          "Invalid email or password", ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
    }

    User user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(
                () ->
                    new BusinessException(
                        "Invalid email or password",
                        ErrorCode.INVALID_CREDENTIALS,
                        HttpStatus.UNAUTHORIZED));

    return new AuthResponse(
        jwtService.generateAccessToken(user), jwtService.generateRefreshToken(user));
  }

  public AuthResponse refresh(String refreshToken) {
    try {
      // Punkt 1: Püüame kinni kõik JWT-ga seotud vead (vigane süntaks, vale allkiri jne)
      Claims claims = jwtService.extractClaims(refreshToken);

      String type = claims.get("type", String.class);
      if (!"refresh".equals(type)) {
        // Punkt 2: Kasutame järjepidevalt BusinessExceptionit
        throw new BusinessException(
            "Invalid token type", ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
      }

      String email = claims.getSubject();
      User user =
          userRepository
              .findByEmail(email)
              .orElseThrow(
                  () ->
                      new BusinessException(
                          "User not found",
                          ErrorCode.INVALID_CREDENTIALS,
                          HttpStatus.UNAUTHORIZED));

      if (!jwtService.isTokenValid(refreshToken, user)) {
        throw new BusinessException(
            "Token is invalid or expired", ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
      }

      String newAccessToken = jwtService.generateAccessToken(user);

      // Tagastame uue access tokeni ja vana refresh tokeni (või genereerime ka uue refresh tokeni,
      // kui soovid)
      return new AuthResponse(newAccessToken, refreshToken);

    } catch (Exception e) {
      // Punkt 3: Kui extractClaims viskab vea, tagastame viisaka 401, mitte 500
      throw new BusinessException(
          "Invalid or malformed token", ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
    }
  }
}
