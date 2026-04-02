package com.caffeine.acs_backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.enums.UserRole;
import com.caffeine.acs_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private JwtService jwtService;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  private User visitor;
  private User admin;

  @BeforeEach
  void setup() {
    userRepository.deleteAll();

    visitor =
        User.builder()
            .email("visitor@example.com")
            .password(passwordEncoder.encode("password"))
            .role(UserRole.VISITOR)
            .build();

    admin =
        User.builder()
            .email("admin@example.com")
            .password(passwordEncoder.encode("password"))
            .role(UserRole.ADMIN)
            .build();

    userRepository.save(visitor);
    userRepository.save(admin);
  }

  @Test
  void protectedEndpoint_withoutToken_returns401() throws Exception {
    mockMvc.perform(get("/api/protected")).andExpect(status().isUnauthorized());
  }

  @Test
  void protectedEndpoint_withMalformedToken_returns401() throws Exception {
    mockMvc
        .perform(get("/api/protected").header("Authorization", "Bearer this-is-not-a-valid-token"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void adminEndpoint_withVisitorRole_returns403() throws Exception {
    String token = jwtService.generateToken(visitor);

    mockMvc
        .perform(get("/api/admin").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminEndpoint_withAdminRole_returns200() throws Exception {
    String token = jwtService.generateToken(admin);

    mockMvc
        .perform(get("/api/admin").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }
}
