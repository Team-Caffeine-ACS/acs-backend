package com.caffeine.acs_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.caffeine.acs_backend.dto.visitgroup.CreateGroupVisitRequest;
import com.caffeine.acs_backend.dto.visitgroup.GroupMemberResponse;
import com.caffeine.acs_backend.dto.visitgroup.GroupVisitListItemResponse;
import com.caffeine.acs_backend.dto.visitgroup.GroupVisitResponse;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.security.JwtAccessDeniedHandler;
import com.caffeine.acs_backend.security.JwtAuthFilter;
import com.caffeine.acs_backend.security.JwtAuthenticationEntryPoint;
import com.caffeine.acs_backend.security.JwtService;
import com.caffeine.acs_backend.security.SecurityConfig;
import com.caffeine.acs_backend.service.VisitGroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VisitGroupController.class)
@Import({
  SecurityConfig.class,
  JwtAuthFilter.class,
  JwtAccessDeniedHandler.class,
  JwtAuthenticationEntryPoint.class
})
class VisitGroupControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private VisitGroupService visitGroupService;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private UserDetailsService userDetailsService;

  // ── Helpers ──────────────────────────────────────────────────────────────────

  private static final UUID GROUP_IN_VISIT_ID = UUID.randomUUID();
  private static final UUID VISIT_ID_1 = UUID.randomUUID();
  private static final UUID VISIT_ID_2 = UUID.randomUUID();
  private static final UUID PERSON_ID_1 = UUID.randomUUID();
  private static final UUID PERSON_ID_2 = UUID.randomUUID();
  private static final UUID BUILDING_ID = UUID.randomUUID();
  private static final LocalDateTime ARRIVAL = LocalDateTime.of(2026, 5, 2, 10, 0);
  private static final LocalDateTime EXIT = LocalDateTime.of(2026, 5, 2, 18, 0);

  private GroupVisitResponse sampleGroupResponse() {
    return new GroupVisitResponse(
        GROUP_IN_VISIT_ID,
        "Queen Band",
        "Rock band",
        ARRIVAL,
        EXIT,
        "Studio tour",
        "Main Entrance",
        "Alice Admin",
        2,
        0,
        0,
        List.of(
            new GroupMemberResponse(
                VISIT_ID_1,
                PERSON_ID_1,
                "Brian May",
                "brian@test.com",
                null,
                VisitStatus.PRE_REGISTERED,
                ARRIVAL,
                null),
            new GroupMemberResponse(
                VISIT_ID_2,
                PERSON_ID_2,
                "Roger Taylor",
                "roger@test.com",
                null,
                VisitStatus.PRE_REGISTERED,
                ARRIVAL,
                null)));
  }

  private GroupVisitListItemResponse sampleListItem() {
    return new GroupVisitListItemResponse(
        GROUP_IN_VISIT_ID, "Queen Band", ARRIVAL, EXIT, "Studio tour", 4, 0, 0);
  }

  private String sampleCreateRequestJson() throws Exception {
    return objectMapper.writeValueAsString(
        new CreateGroupVisitRequest(
            "Queen Band",
            "Rock band",
            List.of(PERSON_ID_1, PERSON_ID_2),
            ARRIVAL,
            EXIT,
            null,
            BUILDING_ID,
            "Studio tour"));
  }

  // ── POST /api/visit-groups ───────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void create_admin_returns201() throws Exception {
    when(visitGroupService.create(any())).thenReturn(sampleGroupResponse());

    mockMvc
        .perform(
            post("/api/visit-groups")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleCreateRequestJson()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.groupName").value("Queen Band"))
        .andExpect(jsonPath("$.memberCount").value(2))
        .andExpect(jsonPath("$.members[0].fullName").value("Brian May"));
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void create_receptionist_returns201() throws Exception {
    when(visitGroupService.create(any())).thenReturn(sampleGroupResponse());

    mockMvc
        .perform(
            post("/api/visit-groups")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleCreateRequestJson()))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser(roles = "SECURITY_CHIEF")
  void create_securityChief_returns201() throws Exception {
    when(visitGroupService.create(any())).thenReturn(sampleGroupResponse());

    mockMvc
        .perform(
            post("/api/visit-groups")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleCreateRequestJson()))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void create_visitor_returns403() throws Exception {
    mockMvc
        .perform(
            post("/api/visit-groups")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleCreateRequestJson()))
        .andExpect(status().isForbidden());
  }

  @Test
  void create_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(
            post("/api/visit-groups")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(sampleCreateRequestJson()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void create_emptyPersonIds_returns400() throws Exception {
    String badRequest =
        objectMapper.writeValueAsString(
            new CreateGroupVisitRequest(
                "Empty", null, List.of(), ARRIVAL, null, null, BUILDING_ID, null));

    mockMvc
        .perform(
            post("/api/visit-groups")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(badRequest))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void create_missingGroupName_returns400() throws Exception {
    String json =
        "{\"personIds\":[\""
            + PERSON_ID_1
            + "\"],"
            + "\"expectedArrival\":\"2026-05-02T10:00:00\","
            + "\"buildingId\":\""
            + BUILDING_ID
            + "\"}";

    mockMvc
        .perform(
            post("/api/visit-groups")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isBadRequest());
  }

  // ── GET /api/visit-groups/{id} ───────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void getById_returns200() throws Exception {
    when(visitGroupService.getById(GROUP_IN_VISIT_ID)).thenReturn(sampleGroupResponse());

    mockMvc
        .perform(get("/api/visit-groups/" + GROUP_IN_VISIT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.groupName").value("Queen Band"))
        .andExpect(jsonPath("$.members").isArray())
        .andExpect(jsonPath("$.members.length()").value(2));
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void getById_receptionist_returns200() throws Exception {
    when(visitGroupService.getById(GROUP_IN_VISIT_ID)).thenReturn(sampleGroupResponse());

    mockMvc.perform(get("/api/visit-groups/" + GROUP_IN_VISIT_ID)).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void getById_visitor_returns403() throws Exception {
    mockMvc
        .perform(get("/api/visit-groups/" + GROUP_IN_VISIT_ID))
        .andExpect(status().isForbidden());
  }

  @Test
  void getById_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(get("/api/visit-groups/" + GROUP_IN_VISIT_ID))
        .andExpect(status().isUnauthorized());
  }

  // ── GET /api/visit-groups ────────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void getAll_returns200WithPage() throws Exception {
    when(visitGroupService.getAll(any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(sampleListItem())));

    mockMvc
        .perform(get("/api/visit-groups").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].groupName").value("Queen Band"))
        .andExpect(jsonPath("$.content[0].memberCount").value(4));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getAll_withSearch_passes200() throws Exception {
    when(visitGroupService.getAll(any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    mockMvc.perform(get("/api/visit-groups").param("search", "Queen")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void getAll_visitor_returns403() throws Exception {
    mockMvc.perform(get("/api/visit-groups")).andExpect(status().isForbidden());
  }

  // ── DELETE /api/visit-groups/{id} ────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void cancel_admin_returns204() throws Exception {
    doNothing().when(visitGroupService).cancel(GROUP_IN_VISIT_ID);

    mockMvc
        .perform(delete("/api/visit-groups/" + GROUP_IN_VISIT_ID).with(csrf()))
        .andExpect(status().isNoContent());

    verify(visitGroupService).cancel(GROUP_IN_VISIT_ID);
  }

  @Test
  @WithMockUser(roles = "SECURITY_CHIEF")
  void cancel_securityChief_returns204() throws Exception {
    doNothing().when(visitGroupService).cancel(GROUP_IN_VISIT_ID);

    mockMvc
        .perform(delete("/api/visit-groups/" + GROUP_IN_VISIT_ID).with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void cancel_receptionist_returns403() throws Exception {
    mockMvc
        .perform(delete("/api/visit-groups/" + GROUP_IN_VISIT_ID).with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void cancel_visitor_returns403() throws Exception {
    mockMvc
        .perform(delete("/api/visit-groups/" + GROUP_IN_VISIT_ID).with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void cancel_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(delete("/api/visit-groups/" + GROUP_IN_VISIT_ID).with(csrf()))
        .andExpect(status().isUnauthorized());
  }
}
