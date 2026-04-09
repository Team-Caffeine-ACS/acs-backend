package com.caffeine.acs_backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.caffeine.acs_backend.dto.auth.LoginRequest;
import com.caffeine.acs_backend.dto.auth.RefreshRequest;
import com.caffeine.acs_backend.dto.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    // ── Register ────────────────────────────────────────────────────────────────

    @Test
    void register_validRequest_returns200WithToken() throws Exception {
        var request = new RegisterRequest(uniqueEmail(), "password123");

        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void register_duplicateEmail_returnsError() throws Exception {
        String email = uniqueEmail();
        var request = new RegisterRequest(email, "password123");
        String body = objectMapper.writeValueAsString(request);

        mockMvc
            .perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk());

        mockMvc
            .perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict());
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        var request = new RegisterRequest("not-an-email", "password123");

        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void register_blankPassword_returns400() throws Exception {
        var request = new RegisterRequest(uniqueEmail(), "");

        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    // ── Login ───────────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        String email = uniqueEmail();
        registerUser(email, "password123");

        var loginRequest = new LoginRequest(email, "password123");

        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        String email = uniqueEmail();
        registerUser(email, "correct-password");

        var loginRequest = new LoginRequest(email, "wrong-password");

        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized());
    }

    // ── Refresh ─────────────────────────────────────────────────────────────────

    @Test
    void refresh_validRefreshToken_returns200WithNewAccessToken() throws Exception {
        String email = uniqueEmail();
        String[] tokens = registerUser(email, "password123");

        var request = new RefreshRequest(tokens[1]);

        mockMvc
            .perform(
                post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void refresh_invalidToken_returns401() throws Exception {
        var request = new RefreshRequest("invalid.token.here");

        mockMvc
            .perform(
                post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_blankToken_returns400() throws Exception {
        var request = new RefreshRequest("");

        mockMvc
            .perform(
                post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    // ── Protected endpoints ─────────────────────────────────────────────────────

    @Test
    void protectedEndpoint_withValidToken_returns200() throws Exception {
        String email = uniqueEmail();
        String[] tokens = registerUser(email, "password123");
        String accessToken = tokens[0];

        mockMvc
            .perform(get("/api/users/me").header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());
    }

    @Test
    void register_assignsVisitorRole() throws Exception {
        String email = uniqueEmail();
        String[] tokens = registerUser(email, "password123");
        String accessToken = tokens[0];

        String responseBody =
            mockMvc
                .perform(get("/api/users/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(responseBody).containsIgnoringCase("VISITOR");
    }

    // ── OpenAPI / Swagger endpoints ─────────────────────────────────────────────

    @Test
    void openApiDocs_withoutToken_returns200() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void swaggerUi_withoutToken_returns200() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private String[] registerUser(String email, String password) throws Exception {
        var request = new RegisterRequest(email, password);
        String responseBody =
            mockMvc
                .perform(
                    post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var tree = objectMapper.readTree(responseBody);
        return new String[] {tree.get("accessToken").asText(), tree.get("refreshToken").asText()};
    }
}