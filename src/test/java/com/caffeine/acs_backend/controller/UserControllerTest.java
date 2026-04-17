package com.caffeine.acs_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.caffeine.acs_backend.dto.user.UpdateUserRequest;
import com.caffeine.acs_backend.dto.user.UserResponse;
import com.caffeine.acs_backend.enums.UserRole;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.security.JwtAccessDeniedHandler;
import com.caffeine.acs_backend.security.JwtAuthFilter;
import com.caffeine.acs_backend.security.JwtAuthenticationEntryPoint;
import com.caffeine.acs_backend.security.JwtService;
import com.caffeine.acs_backend.security.SecurityConfig;
import com.caffeine.acs_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({
  SecurityConfig.class,
  JwtAuthFilter.class,
  JwtAccessDeniedHandler.class,
  JwtAuthenticationEntryPoint.class
})
class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserService userService;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private UserDetailsService userDetailsService;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private UserResponse sampleResponse(String email, UserRole role) {
    return new UserResponse(USER_ID, email, role, null);
  }

  // ── GET /api/users/me ─────────────────────────────────────────────────────────

  @Test
  @WithMockUser
  void getMe_authenticated_returns200() throws Exception {
    when(userService.getMe(any())).thenReturn(sampleResponse("alice@example.com", UserRole.VISITOR));

    mockMvc
        .perform(get("/api/users/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("alice@example.com"))
        .andExpect(jsonPath("$.role").value("VISITOR"));
  }

  @Test
  void getMe_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
  }

  // ── PATCH /api/users/me ───────────────────────────────────────────────────────

  @Test
  @WithMockUser
  void updateMe_validEmail_returns200() throws Exception {
    when(userService.updateMe(any(), any()))
        .thenReturn(sampleResponse("new@example.com", UserRole.VISITOR));

    mockMvc
        .perform(
            patch("/api/users/me")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("new@example.com", null))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("new@example.com"));
  }

  @Test
  @WithMockUser
  void updateMe_invalidEmailFormat_returns400() throws Exception {
    mockMvc
        .perform(
            patch("/api/users/me")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("not-an-email", null))))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void updateMe_duplicateEmail_returns409() throws Exception {
    when(userService.updateMe(any(), any()))
        .thenThrow(
            new BusinessException(
                "Email already in use", ErrorCode.RESOURCE_ALREADY_EXISTS, HttpStatus.CONFLICT));

    mockMvc
        .perform(
            patch("/api/users/me")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("taken@example.com", null))))
        .andExpect(status().isConflict());
  }

  @Test
  void updateMe_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(
            patch("/api/users/me")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("a@b.com", null))))
        .andExpect(status().isUnauthorized());
  }

  // ── PUT /api/users/admin/{id} ─────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminUpdateUser_admin_returns200() throws Exception {
    when(userService.adminUpdateUser(eq(USER_ID), any()))
        .thenReturn(sampleResponse("updated@example.com", UserRole.VISITOR));

    mockMvc
        .perform(
            put("/api/users/admin/{id}", USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("updated@example.com", null))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("updated@example.com"));
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void adminUpdateUser_nonAdmin_returns403() throws Exception {
    mockMvc
        .perform(
            put("/api/users/admin/{id}", USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("a@b.com", null))))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminUpdateUser_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(
            put("/api/users/admin/{id}", USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("a@b.com", null))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminUpdateUser_userNotFound_returns404() throws Exception {
    when(userService.adminUpdateUser(eq(USER_ID), any()))
        .thenThrow(
            new BusinessException("User not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));

    mockMvc
        .perform(
            put("/api/users/admin/{id}", USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("a@b.com", null))))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminUpdateUser_invalidEmailFormat_returns400() throws Exception {
    mockMvc
        .perform(
            put("/api/users/admin/{id}", USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("not-an-email", null))))
        .andExpect(status().isBadRequest());
  }
}
