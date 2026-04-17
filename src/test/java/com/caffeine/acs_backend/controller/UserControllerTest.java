package com.caffeine.acs_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.caffeine.acs_backend.dto.user.AdminCreateUserRequest;
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
import java.util.List;
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
    return new UserResponse(USER_ID, email, role, null, null);
  }

  // ── GET /api/users/me ─────────────────────────────────────────────────────────

  @Test
  @WithMockUser
  void getMe_authenticated_returns200() throws Exception {
    when(userService.getMe(any()))
        .thenReturn(sampleResponse("alice@example.com", UserRole.VISITOR));

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
                .content(
                    objectMapper.writeValueAsString(
                        new UpdateUserRequest("new@example.com", null))))
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
                .content(
                    objectMapper.writeValueAsString(new UpdateUserRequest("not-an-email", null))))
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
                .content(
                    objectMapper.writeValueAsString(
                        new UpdateUserRequest("taken@example.com", null))))
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

  // ── GET /api/users/admin ──────────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminGetAllUsers_admin_returns200WithList() throws Exception {
    when(userService.adminGetAllUsers())
        .thenReturn(
            List.of(
                sampleResponse("alice@example.com", UserRole.VISITOR),
                sampleResponse("bob@example.com", UserRole.RECEPTIONIST)));

    mockMvc
        .perform(get("/api/users/admin"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].email").value("alice@example.com"))
        .andExpect(jsonPath("$[1].email").value("bob@example.com"));
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void adminGetAllUsers_nonAdmin_returns403() throws Exception {
    mockMvc.perform(get("/api/users/admin")).andExpect(status().isForbidden());
  }

  @Test
  void adminGetAllUsers_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/users/admin")).andExpect(status().isUnauthorized());
  }

  // ── POST /api/users/admin ─────────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCreateUser_admin_returns201() throws Exception {
    when(userService.adminCreateUser(any()))
        .thenReturn(sampleResponse("new@example.com", UserRole.VISITOR));

    mockMvc
        .perform(
            post("/api/users/admin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new AdminCreateUserRequest("new@example.com", "Password1!", null))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("new@example.com"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCreateUser_invalidEmail_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/users/admin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new AdminCreateUserRequest("not-an-email", "Password1!", null))))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCreateUser_duplicateEmail_returns409() throws Exception {
    when(userService.adminCreateUser(any()))
        .thenThrow(
            new BusinessException(
                "Email already in use", ErrorCode.RESOURCE_ALREADY_EXISTS, HttpStatus.CONFLICT));

    mockMvc
        .perform(
            post("/api/users/admin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new AdminCreateUserRequest("taken@example.com", "Password1!", null))))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void adminCreateUser_nonAdmin_returns403() throws Exception {
    mockMvc
        .perform(
            post("/api/users/admin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new AdminCreateUserRequest("new@example.com", "Password1!", null))))
        .andExpect(status().isForbidden());
  }

  // ── DELETE /api/users/admin/{id} ──────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminDeleteUser_admin_returns204() throws Exception {
    mockMvc
        .perform(delete("/api/users/admin/{id}", USER_ID).with(csrf()))
        .andExpect(status().isNoContent());

    verify(userService).adminDeleteUser(USER_ID);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminDeleteUser_userNotFound_returns404() throws Exception {
    doThrow(
            new BusinessException(
                "User not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND))
        .when(userService)
        .adminDeleteUser(USER_ID);

    mockMvc
        .perform(delete("/api/users/admin/{id}", USER_ID).with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void adminDeleteUser_nonAdmin_returns403() throws Exception {
    mockMvc
        .perform(delete("/api/users/admin/{id}", USER_ID).with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminDeleteUser_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(delete("/api/users/admin/{id}", USER_ID).with(csrf()))
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
                .content(
                    objectMapper.writeValueAsString(
                        new UpdateUserRequest("updated@example.com", null))))
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
            new BusinessException(
                "User not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));

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
                .content(
                    objectMapper.writeValueAsString(new UpdateUserRequest("not-an-email", null))))
        .andExpect(status().isBadRequest());
  }
}
