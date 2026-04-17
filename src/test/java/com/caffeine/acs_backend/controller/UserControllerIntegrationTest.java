package com.caffeine.acs_backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.caffeine.acs_backend.dto.auth.LoginRequest;
import com.caffeine.acs_backend.dto.auth.RegisterRequest;
import com.caffeine.acs_backend.dto.user.UpdateUserRequest;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.enums.UserRole;
import com.caffeine.acs_backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private String adminToken;

  private String uniqueEmail() {
    return "user-" + UUID.randomUUID() + "@example.com";
  }

  @BeforeEach
  void createAdminUser() throws Exception {
    String adminEmail = "admin-" + UUID.randomUUID() + "@example.com";
    User admin =
        User.builder()
            .email(adminEmail)
            .password(passwordEncoder.encode("password"))
            .role(UserRole.ADMIN)
            .build();
    userRepository.save(admin);
    adminToken = loginAndGetToken(adminEmail, "password");
  }

  private String[] registerUser(String email, String password) throws Exception {
    var request = new RegisterRequest(email, password);
    String body =
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    var tree = objectMapper.readTree(body);
    return new String[] {tree.get("accessToken").asText(), tree.get("refreshToken").asText()};
  }

  private String loginAndGetToken(String email, String password) throws Exception {
    var request = new LoginRequest(email, password);
    String body =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    return objectMapper.readTree(body).get("accessToken").asText();
  }

  // ── GET /api/users/me ─────────────────────────────────────────────────────────

  @Test
  void getMe_withValidToken_returns200WithEmailAndRole() throws Exception {
    String email = uniqueEmail();
    String token = registerUser(email, "password123")[0];

    mockMvc
        .perform(get("/api/users/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.role").value("VISITOR"));
  }

  @Test
  void getMe_noToken_returns401() throws Exception {
    mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
  }

  // ── PATCH /api/users/me ───────────────────────────────────────────────────────

  @Test
  void updateMe_changeEmail_returns200WithNewEmail() throws Exception {
    String oldEmail = uniqueEmail();
    String newEmail = uniqueEmail();
    String token = registerUser(oldEmail, "password123")[0];

    mockMvc
        .perform(
            patch("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest(newEmail, null))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(newEmail));
  }

  @Test
  void updateMe_changePassword_canLoginWithNewPassword() throws Exception {
    String email = uniqueEmail();
    registerUser(email, "oldpassword");
    String token = loginAndGetToken(email, "oldpassword");

    mockMvc
        .perform(
            patch("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new UpdateUserRequest(null, "newpassword123"))))
        .andExpect(status().isOk());

    // old password no longer works
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(email, "oldpassword"))))
        .andExpect(status().isUnauthorized());

    // new password works
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new LoginRequest(email, "newpassword123"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty());
  }

  @Test
  void updateMe_duplicateEmail_returns409() throws Exception {
    String takenEmail = uniqueEmail();
    registerUser(takenEmail, "password123");

    String otherEmail = uniqueEmail();
    String token = registerUser(otherEmail, "password123")[0];

    mockMvc
        .perform(
            patch("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest(takenEmail, null))))
        .andExpect(status().isConflict());
  }

  @Test
  void updateMe_invalidEmailFormat_returns400() throws Exception {
    String token = registerUser(uniqueEmail(), "password123")[0];

    mockMvc
        .perform(
            patch("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new UpdateUserRequest("not-an-email", null))))
        .andExpect(status().isBadRequest());
  }

  // ── PUT /api/users/admin/{id} ─────────────────────────────────────────────────

  @Test
  void adminUpdateUser_adminChangesEmail_returns200() throws Exception {
    String email = uniqueEmail();
    registerUser(email, "password123");
    UUID userId = userRepository.findByEmail(email).orElseThrow().getId();
    String newEmail = uniqueEmail();

    mockMvc
        .perform(
            put("/api/users/admin/{id}", userId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest(newEmail, null))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(newEmail))
        .andExpect(jsonPath("$.id").value(userId.toString()));
  }

  @Test
  void adminUpdateUser_adminChangesPassword_userCanLoginWithNewPassword() throws Exception {
    String email = uniqueEmail();
    registerUser(email, "oldpassword");
    UUID userId = userRepository.findByEmail(email).orElseThrow().getId();

    mockMvc
        .perform(
            put("/api/users/admin/{id}", userId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new UpdateUserRequest(null, "adminsetpass"))))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(email, "adminsetpass"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty());
  }

  @Test
  void adminUpdateUser_nonAdmin_returns403() throws Exception {
    String targetEmail = uniqueEmail();
    registerUser(targetEmail, "password123");
    UUID userId = userRepository.findByEmail(targetEmail).orElseThrow().getId();

    String visitorToken = registerUser(uniqueEmail(), "password123")[0];

    mockMvc
        .perform(
            put("/api/users/admin/{id}", userId)
                .header("Authorization", "Bearer " + visitorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new UpdateUserRequest("x@example.com", null))))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminUpdateUser_noToken_returns401() throws Exception {
    mockMvc
        .perform(
            put("/api/users/admin/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("a@b.com", null))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void adminUpdateUser_unknownId_returns404() throws Exception {
    mockMvc
        .perform(
            put("/api/users/admin/{id}", UUID.randomUUID())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("a@b.com", null))))
        .andExpect(status().isNotFound());
  }

  @Test
  void adminUpdateUser_responseBodyContainsId() throws Exception {
    String email = uniqueEmail();
    registerUser(email, "password123");
    UUID userId = userRepository.findByEmail(email).orElseThrow().getId();

    String responseBody =
        mockMvc
            .perform(
                put("/api/users/admin/{id}", userId)
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            new UpdateUserRequest(uniqueEmail(), null))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(objectMapper.readTree(responseBody).get("id").asText()).isEqualTo(userId.toString());
  }
}
