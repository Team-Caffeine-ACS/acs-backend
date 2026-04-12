package com.caffeine.acs_backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CorsIntegrationTest {

  @Autowired private MockMvc mockMvc;

  private static final String ALLOWED_ORIGIN = "http://localhost:3000";
  private static final String DISALLOWED_ORIGIN = "http://evil.example.com";

  // ── Preflight (OPTIONS) ───────────────────────────────────────────────────────

  @Test
  void preflight_allowedOrigin_returns200WithCorsHeaders() throws Exception {
    mockMvc
        .perform(
            options("/api/auth/login")
                .header("Origin", ALLOWED_ORIGIN)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type, Authorization"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
        .andExpect(header().exists("Access-Control-Allow-Methods"))
        .andExpect(header().exists("Access-Control-Allow-Headers"));
  }

  @Test
  void preflight_disallowedOrigin_returnsNoCorsHeaders() throws Exception {
    mockMvc
        .perform(
            options("/api/auth/login")
                .header("Origin", DISALLOWED_ORIGIN)
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
  }

  @Test
  void preflight_allowedOrigin_allowsCredentials() throws Exception {
    mockMvc
        .perform(
            options("/api/auth/login")
                .header("Origin", ALLOWED_ORIGIN)
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
  }

  // ── Actual requests ───────────────────────────────────────────────────────────

  @Test
  void actualRequest_allowedOrigin_respondsWithCorsHeader() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .header("Origin", ALLOWED_ORIGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nobody@example.com\",\"password\":\"wrong\"}"))
        .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN));
  }

  @Test
  void actualRequest_disallowedOrigin_returnsNoCorsHeader() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .header("Origin", DISALLOWED_ORIGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nobody@example.com\",\"password\":\"wrong\"}"))
        .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
  }
}
