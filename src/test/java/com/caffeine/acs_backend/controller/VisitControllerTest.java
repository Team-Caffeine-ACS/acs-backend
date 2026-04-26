package com.caffeine.acs_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.caffeine.acs_backend.dto.visit.CreateVisitRequest;
import com.caffeine.acs_backend.dto.visit.CreateVisitResponse;
import com.caffeine.acs_backend.dto.visit.EditVisitRequest;
import com.caffeine.acs_backend.dto.visit.ExitVisitRequest;
import com.caffeine.acs_backend.dto.visit.VisitDetailResponse;
import com.caffeine.acs_backend.dto.visit.VisitListItemResponse;
import com.caffeine.acs_backend.dto.visit.VisitTimelineEntry;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.enums.UserRole;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.security.JwtAccessDeniedHandler;
import com.caffeine.acs_backend.security.JwtAuthFilter;
import com.caffeine.acs_backend.security.JwtAuthenticationEntryPoint;
import com.caffeine.acs_backend.security.JwtService;
import com.caffeine.acs_backend.security.SecurityConfig;
import com.caffeine.acs_backend.service.VisitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VisitController.class)
@Import({
  SecurityConfig.class,
  JwtAuthFilter.class,
  JwtAccessDeniedHandler.class,
  JwtAuthenticationEntryPoint.class
})
class VisitControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private VisitService visitService;

  // JwtAuthFilter dependencies — real filter runs, mock its collaborators
  @MockitoBean private JwtService jwtService;
  @MockitoBean private UserDetailsService userDetailsService;

  private static final UUID VISIT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID PERSON_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  // ── Helpers ──────────────────────────────────────────────────────────────────

  /** Creates a real entity User for @AuthenticationPrincipal endpoints. */
  private User entityUser(UserRole role) {
    return User.builder()
        .email(role.name().toLowerCase() + "@test.local")
        .password("pw")
        .role(role)
        .build();
  }

  private VisitDetailResponse sampleDetail() {
    return new VisitDetailResponse(
        VISIT_ID, "John", "Smith", null, null, null, null, "Test visit", null);
  }

  private CreateVisitResponse sampleCreateResponse() {
    return new CreateVisitResponse(
        VISIT_ID,
        PERSON_ID,
        UUID.randomUUID(),
        "John",
        "Smith",
        null,
        null,
        null,
        null,
        "Test visit",
        LocalDateTime.now(),
        null,
        null,
        null);
  }

  // ── GET /api/visits ──────────────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void getVisits_receptionist_returns200() throws Exception {
    VisitListItemResponse item =
        new VisitListItemResponse(
            VISIT_ID,
            "John Smith",
            null,
            "Acme Corp",
            null,
            LocalDateTime.now(),
            null,
            VisitStatus.IN_BUILDING,
            PERSON_ID,
            UUID.randomUUID(),
            "Test Access Point",
            "123 Test Street"
        );
    when(visitService.getVisits(any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(item)));

    mockMvc
        .perform(get("/api/visits"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].fullName").value("John Smith"))
        .andExpect(jsonPath("$.content[0].status").value(VisitStatus.IN_BUILDING.getLabel()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getVisits_admin_returns200() throws Exception {
    when(visitService.getVisits(any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    mockMvc.perform(get("/api/visits")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void getVisits_visitor_returns403() throws Exception {
    mockMvc.perform(get("/api/visits")).andExpect(status().isForbidden());
  }

  @Test
  void getVisits_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/visits")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void getVisits_withStatusFilter_passesParam() throws Exception {
    when(visitService.getVisits(
            any(), eq(VisitStatus.IN_BUILDING.name()), any(), any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    mockMvc
        .perform(get("/api/visits").param("status", VisitStatus.IN_BUILDING.name()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty());
  }

  // ── GET /api/visits/{visitId} ────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void getVisit_receptionist_returns200() throws Exception {
    when(visitService.getVisit(VISIT_ID)).thenReturn(sampleDetail());

    mockMvc
        .perform(get("/api/visits/{id}", VISIT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.lastName").value("Smith"))
        .andExpect(jsonPath("$.visitId").value(VISIT_ID.toString()));
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void getVisit_visitor_returns403() throws Exception {
    mockMvc.perform(get("/api/visits/{id}", VISIT_ID)).andExpect(status().isForbidden());
  }

  @Test
  void getVisit_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/visits/{id}", VISIT_ID)).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void getVisit_notFound_returns404() throws Exception {
    when(visitService.getVisit(VISIT_ID))
        .thenThrow(
            new BusinessException(
                "Visit not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));

    mockMvc.perform(get("/api/visits/{id}", VISIT_ID)).andExpect(status().isNotFound());
  }

  // ── POST /api/visits ─────────────────────────────────────────────────────────
  // Uses @AuthenticationPrincipal User — must inject real entity User via .with(user(...))

  @Test
  void createVisit_receptionist_returns201() throws Exception {
    CreateVisitRequest request =
        new CreateVisitRequest(
            PERSON_ID, UUID.randomUUID(), null, null, "Test", LocalDateTime.now());
    when(visitService.createVisit(any(), any())).thenReturn(sampleCreateResponse());

    mockMvc
        .perform(
            post("/api/visits")
                .with(user(entityUser(UserRole.RECEPTIONIST)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.visitId").value(VISIT_ID.toString()))
        .andExpect(jsonPath("$.firstName").value("John"));
  }

  @Test
  void createVisit_securityChief_returns201() throws Exception {
    CreateVisitRequest request =
        new CreateVisitRequest(PERSON_ID, UUID.randomUUID(), null, null, null, null);
    when(visitService.createVisit(any(), any())).thenReturn(sampleCreateResponse());

    mockMvc
        .perform(
            post("/api/visits")
                .with(user(entityUser(UserRole.SECURITY_CHIEF)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  void createVisit_visitor_returns403() throws Exception {
    CreateVisitRequest request =
        new CreateVisitRequest(PERSON_ID, UUID.randomUUID(), null, null, null, null);

    mockMvc
        .perform(
            post("/api/visits")
                .with(user(entityUser(UserRole.VISITOR)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  void createVisit_missingRequiredFields_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/visits")
                .with(user(entityUser(UserRole.RECEPTIONIST)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  // ── PUT /api/visits/{visitId}/exit ────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void exitVisit_receptionist_returns200() throws Exception {
    ExitVisitRequest request = new ExitVisitRequest(LocalDateTime.now());
    when(visitService.exitVisit(eq(VISIT_ID), any())).thenReturn(sampleDetail());

    mockMvc
        .perform(
            put("/api/visits/{id}/exit", VISIT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.visitId").value(VISIT_ID.toString()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void exitVisit_admin_returns200() throws Exception {
    ExitVisitRequest request = new ExitVisitRequest(LocalDateTime.now());
    when(visitService.exitVisit(eq(VISIT_ID), any())).thenReturn(sampleDetail());

    mockMvc
        .perform(
            put("/api/visits/{id}/exit", VISIT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void exitVisit_visitor_returns403() throws Exception {
    ExitVisitRequest request = new ExitVisitRequest(LocalDateTime.now());

    mockMvc
        .perform(
            put("/api/visits/{id}/exit", VISIT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void exitVisit_alreadyExited_returns409() throws Exception {
    ExitVisitRequest request = new ExitVisitRequest(LocalDateTime.now());
    when(visitService.exitVisit(eq(VISIT_ID), any()))
        .thenThrow(
            new BusinessException(
                "Visit already has an exit time",
                ErrorCode.BUSINESS_RULE_VIOLATION,
                HttpStatus.CONFLICT));

    mockMvc
        .perform(
            put("/api/visits/{id}/exit", VISIT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void exitVisit_missingExitTime_returns400() throws Exception {
    mockMvc
        .perform(
            put("/api/visits/{id}/exit", VISIT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  // ── PUT /api/visits/{visitId}/edit ────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void editVisit_admin_returns200() throws Exception {
    EditVisitRequest request =
        new EditVisitRequest(
            null, UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), "Updated");
    when(visitService.editVisit(eq(VISIT_ID), any())).thenReturn(sampleDetail());

    mockMvc
        .perform(
            put("/api/visits/{id}/edit", VISIT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.visitId").value(VISIT_ID.toString()));
  }

  @Test
  @WithMockUser(roles = "SECURITY_CHIEF")
  void editVisit_securityChief_returns200() throws Exception {
    EditVisitRequest request =
        new EditVisitRequest(null, UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), null);
    when(visitService.editVisit(eq(VISIT_ID), any())).thenReturn(sampleDetail());

    mockMvc
        .perform(
            put("/api/visits/{id}/edit", VISIT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {"RECEPTIONIST", "VISITOR"})
  void editVisit_forbiddenRoles_return403(String role) throws Exception {
    EditVisitRequest request =
        new EditVisitRequest(null, UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), null);

    mockMvc
        .perform(
            put("/api/visits/{id}/edit", VISIT_ID)
                .with(user(role.toLowerCase()).roles(role))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void editVisit_notFound_returns404() throws Exception {
    EditVisitRequest request =
        new EditVisitRequest(null, UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), null);
    when(visitService.editVisit(eq(VISIT_ID), any()))
        .thenThrow(
            new BusinessException(
                "Visit not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));

    mockMvc
        .perform(
            put("/api/visits/{id}/edit", VISIT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void editVisit_entryTimeConflict_returns409() throws Exception {
    EditVisitRequest request =
        new EditVisitRequest(null, UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), null);
    when(visitService.editVisit(eq(VISIT_ID), any()))
        .thenThrow(
            new BusinessException(
                "Entry time cannot be later than the recorded exit time",
                ErrorCode.BUSINESS_RULE_VIOLATION,
                HttpStatus.CONFLICT));

    mockMvc
        .perform(
            put("/api/visits/{id}/edit", VISIT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void editVisit_missingRequiredFields_returns400() throws Exception {
    mockMvc
        .perform(
            put("/api/visits/{id}/edit", VISIT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  // ── GET /api/visits/{visitId}/timeline ────────────────────────────────────────

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void getTimeline_receptionist_returns200() throws Exception {
    List<VisitTimelineEntry> entries =
        List.of(
            new VisitTimelineEntry(
                LocalDateTime.now().minusHours(2), "ARRIVAL_REGISTERED", "Test visit"),
            new VisitTimelineEntry(
                LocalDateTime.now().minusMinutes(30), "DEPARTURE_REGISTERED", null));
    when(visitService.getTimeline(VISIT_ID)).thenReturn(entries);

    mockMvc
        .perform(get("/api/visits/{id}/timeline", VISIT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].eventType").value("ARRIVAL_REGISTERED"))
        .andExpect(jsonPath("$[1].eventType").value("DEPARTURE_REGISTERED"));
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void getTimeline_visitor_returns403() throws Exception {
    mockMvc.perform(get("/api/visits/{id}/timeline", VISIT_ID)).andExpect(status().isForbidden());
  }

  @Test
  void getTimeline_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(get("/api/visits/{id}/timeline", VISIT_ID))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void getTimeline_notFound_returns404() throws Exception {
    when(visitService.getTimeline(VISIT_ID))
        .thenThrow(
            new BusinessException(
                "Visit not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));

    mockMvc.perform(get("/api/visits/{id}/timeline", VISIT_ID)).andExpect(status().isNotFound());
  }
}
