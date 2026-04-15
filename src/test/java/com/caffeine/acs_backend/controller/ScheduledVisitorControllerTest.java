package com.caffeine.acs_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.caffeine.acs_backend.dto.visitor.CreateScheduledVisitorRequest;
import com.caffeine.acs_backend.dto.visitor.ScheduledVisitorResponse;
import com.caffeine.acs_backend.dto.visitor.UpdateStatusRequest;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.enums.UserRole;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.security.JwtAccessDeniedHandler;
import com.caffeine.acs_backend.security.JwtAuthFilter;
import com.caffeine.acs_backend.security.JwtAuthenticationEntryPoint;
import com.caffeine.acs_backend.security.JwtService;
import com.caffeine.acs_backend.security.SecurityConfig;
import com.caffeine.acs_backend.service.ScheduledVisitorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScheduledVisitorController.class)
@Import({
  SecurityConfig.class,
  JwtAuthFilter.class,
  JwtAccessDeniedHandler.class,
  JwtAuthenticationEntryPoint.class
})
class ScheduledVisitorControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private ScheduledVisitorService service;

  @MockBean private JwtService jwtService;
  @MockBean private UserDetailsService userDetailsService;

  private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  // ─── helper ─────────────────────────────────────────────────

  private User testUser(UserRole role) {
    return User.builder()
        .email(role.name().toLowerCase() + "@test.com")
        .password("pw")
        .role(role)
        .build();
  }

  private ScheduledVisitorResponse sample() {
    return new ScheduledVisitorResponse(
        ID, LocalDateTime.now(), "John Doe", "PASSPORT", "Admin", VisitStatus.PRE_REGISTERED);
  }

  // ─── GET /scheduled-visitors ────────────────────────────────

  @Test
  void shouldReturnAllVisitors() throws Exception {
    when(service.getAll(any(), any(), any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(java.util.List.of(sample())));

    mockMvc
        .perform(get("/scheduled-visitors").with(user(testUser(UserRole.RECEPTIONIST))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].fullName").value("John Doe"));
  }

  // ─── GET BY ID ──────────────────────────────────────────────

  @Test
  void shouldGetById() throws Exception {
    when(service.getById(ID)).thenReturn(sample());

    mockMvc
        .perform(get("/scheduled-visitors/{id}", ID).with(user(testUser(UserRole.RECEPTIONIST))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.visitorId").value(ID.toString()));
  }

  // ─── CREATE ─────────────────────────────────────────────────

  @Test
  void shouldCreateVisitor() throws Exception {
    CreateScheduledVisitorRequest request = new CreateScheduledVisitorRequest();
    request.setPersonId(UUID.randomUUID());
    request.setBuildingId(UUID.randomUUID());
    request.setScheduledTime(LocalDateTime.now());

    when(service.create(any())).thenReturn(ID);

    mockMvc
        .perform(
            post("/scheduled-visitors")
                .with(user(testUser(UserRole.RECEPTIONIST)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  // ─── UPDATE STATUS ──────────────────────────────────────────

  @Test
  void shouldUpdateStatus() throws Exception {
    UpdateStatusRequest req = new UpdateStatusRequest();
    req.setStatus(VisitStatus.CANCELLED);

    mockMvc
        .perform(
            put("/scheduled-visitors/{id}/status", ID)
                .with(user(testUser(UserRole.RECEPTIONIST)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk());
  }

  // ─── STATS ──────────────────────────────────────────────────

  @Test
  void shouldReturnStats() throws Exception {
    when(service.getStats()).thenReturn(Map.of("todayVisitors", 5L));

    mockMvc
        .perform(get("/scheduled-visitors/stats").with(user(testUser(UserRole.RECEPTIONIST))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.todayVisitors").value(5));
  }

  // ─── EXPORT ─────────────────────────────────────────────────

  @Test
  void shouldExport() throws Exception {
    when(service.export(any(), any(), any(), any())).thenReturn(new byte[] {1, 2, 3});

    mockMvc
        .perform(get("/scheduled-visitors/export").with(user(testUser(UserRole.RECEPTIONIST))))
        .andExpect(status().isOk())
        .andExpect(header().exists("Content-Disposition"));
  }
}
