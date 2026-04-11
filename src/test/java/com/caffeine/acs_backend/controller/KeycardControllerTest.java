package com.caffeine.acs_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.caffeine.acs_backend.dto.keycard.AssignKeycardRequest;
import com.caffeine.acs_backend.dto.keycard.CreateKeycardRequest;
import com.caffeine.acs_backend.dto.keycard.KeycardDetailResponse;
import com.caffeine.acs_backend.dto.keycard.KeycardListItemResponse;
import com.caffeine.acs_backend.dto.keycard.ReturnKeycardRequest;
import com.caffeine.acs_backend.dto.keycard.UpdateKeycardRequest;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.security.JwtAccessDeniedHandler;
import com.caffeine.acs_backend.security.JwtAuthFilter;
import com.caffeine.acs_backend.security.JwtAuthenticationEntryPoint;
import com.caffeine.acs_backend.security.JwtService;
import com.caffeine.acs_backend.security.SecurityConfig;
import com.caffeine.acs_backend.service.KeycardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(KeycardController.class)
@Import({
  SecurityConfig.class,
  JwtAuthFilter.class,
  JwtAccessDeniedHandler.class,
  JwtAuthenticationEntryPoint.class
})
class KeycardControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private KeycardService keycardService;

  // JwtAuthFilter dependencies — real filter runs, mock its collaborators
  @MockBean private JwtService jwtService;
  @MockBean private UserDetailsService userDetailsService;

  private static final UUID CARD_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private KeycardDetailResponse sampleDetail() {
    return new KeycardDetailResponse(CARD_ID, "KC-0001", true, null, null, null, null, null);
  }

  // ── GET /api/keycards ────────────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void getKeycards_receptionist_returns200() throws Exception {
    KeycardListItemResponse item =
        new KeycardListItemResponse(CARD_ID, "KC-0001", "AVAILABLE", null, null);
    when(keycardService.getKeycards(any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(item)));

    mockMvc
        .perform(get("/api/keycards"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].keycardNumber").value("KC-0001"))
        .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getKeycards_admin_returns200() throws Exception {
    when(keycardService.getKeycards(any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    mockMvc.perform(get("/api/keycards")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void getKeycards_visitor_returns403() throws Exception {
    mockMvc.perform(get("/api/keycards")).andExpect(status().isForbidden());
  }

  @Test
  void getKeycards_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/keycards")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void getKeycards_withStatusFilter_passes() throws Exception {
    when(keycardService.getKeycards(any(), eq("in_use"), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    mockMvc
        .perform(get("/api/keycards").param("status", "in_use"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty());
  }

  // ── GET /api/keycards/{cardId} ───────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void getKeycard_receptionist_returns200() throws Exception {
    when(keycardService.getKeycard(CARD_ID)).thenReturn(sampleDetail());

    mockMvc
        .perform(get("/api/keycards/{id}", CARD_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keycardNumber").value("KC-0001"))
        .andExpect(jsonPath("$.active").value(true));
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void getKeycard_visitor_returns403() throws Exception {
    mockMvc.perform(get("/api/keycards/{id}", CARD_ID)).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void getKeycard_notFound_returns404() throws Exception {
    when(keycardService.getKeycard(CARD_ID))
        .thenThrow(
            new BusinessException(
                "Keycard not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));

    mockMvc.perform(get("/api/keycards/{id}", CARD_ID)).andExpect(status().isNotFound());
  }

  // ── POST /api/keycards ───────────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void createKeycard_admin_returns201() throws Exception {
    CreateKeycardRequest request = new CreateKeycardRequest("KC-0099", null, true);
    when(keycardService.createKeycard(any())).thenReturn(sampleDetail());

    mockMvc
        .perform(
            post("/api/keycards")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.keycardNumber").value("KC-0001"));
  }

  @Test
  @WithMockUser(roles = "SECURITY_CHIEF")
  void createKeycard_securityChief_returns201() throws Exception {
    CreateKeycardRequest request = new CreateKeycardRequest("KC-0099", null, true);
    when(keycardService.createKeycard(any())).thenReturn(sampleDetail());

    mockMvc
        .perform(
            post("/api/keycards")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void createKeycard_receptionist_returns403() throws Exception {
    CreateKeycardRequest request = new CreateKeycardRequest("KC-0099", null, true);

    mockMvc
        .perform(
            post("/api/keycards")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createKeycard_duplicateNumber_returns409() throws Exception {
    CreateKeycardRequest request = new CreateKeycardRequest("KC-0001", null, true);
    when(keycardService.createKeycard(any()))
        .thenThrow(
            new BusinessException(
                "Keycard number already exists",
                ErrorCode.RESOURCE_ALREADY_EXISTS,
                HttpStatus.CONFLICT));

    mockMvc
        .perform(
            post("/api/keycards")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createKeycard_blankNumber_returns400() throws Exception {
    CreateKeycardRequest request = new CreateKeycardRequest("", null, true);

    mockMvc
        .perform(
            post("/api/keycards")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  // ── PUT /api/keycards/{cardId} ───────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateKeycard_admin_returns200() throws Exception {
    UpdateKeycardRequest request = new UpdateKeycardRequest("KC-0001-NEW", true, null);
    when(keycardService.updateKeycard(eq(CARD_ID), any())).thenReturn(sampleDetail());

    mockMvc
        .perform(
            put("/api/keycards/{id}", CARD_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(CARD_ID.toString()));
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void updateKeycard_receptionist_returns403() throws Exception {
    UpdateKeycardRequest request = new UpdateKeycardRequest(null, false, null);

    mockMvc
        .perform(
            put("/api/keycards/{id}", CARD_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateKeycard_notFound_returns404() throws Exception {
    UpdateKeycardRequest request = new UpdateKeycardRequest(null, false, null);
    when(keycardService.updateKeycard(eq(CARD_ID), any()))
        .thenThrow(
            new BusinessException(
                "Keycard not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));

    mockMvc
        .perform(
            put("/api/keycards/{id}", CARD_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  // ── POST /api/keycards/{cardId}/assign ───────────────────────────────────────

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void assignKeycard_receptionist_returns200() throws Exception {
    AssignKeycardRequest request =
        new AssignKeycardRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    KeycardDetailResponse withHolder =
        new KeycardDetailResponse(
            CARD_ID,
            "KC-0001",
            true,
            null,
            "Alice Smith",
            UUID.randomUUID(),
            LocalDateTime.now(),
            null);
    when(keycardService.assignKeycard(eq(CARD_ID), any())).thenReturn(withHolder);

    mockMvc
        .perform(
            post("/api/keycards/{id}/assign", CARD_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.assignedUser").value("Alice Smith"));
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void assignKeycard_visitor_returns403() throws Exception {
    AssignKeycardRequest request =
        new AssignKeycardRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

    mockMvc
        .perform(
            post("/api/keycards/{id}/assign", CARD_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void assignKeycard_alreadyAssigned_returns409() throws Exception {
    AssignKeycardRequest request =
        new AssignKeycardRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    when(keycardService.assignKeycard(eq(CARD_ID), any()))
        .thenThrow(
            new BusinessException(
                "Keycard is already assigned",
                ErrorCode.BUSINESS_RULE_VIOLATION,
                HttpStatus.CONFLICT));

    mockMvc
        .perform(
            post("/api/keycards/{id}/assign", CARD_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void assignKeycard_missingBody_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/keycards/{id}/assign", CARD_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  // ── POST /api/keycards/{cardId}/return ───────────────────────────────────────

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void returnKeycard_receptionist_returns200() throws Exception {
    ReturnKeycardRequest request = new ReturnKeycardRequest(UUID.randomUUID());
    LocalDateTime returnTime = LocalDateTime.now();
    KeycardDetailResponse returned =
        new KeycardDetailResponse(CARD_ID, "KC-0001", true, null, null, null, null, returnTime);
    when(keycardService.returnKeycard(eq(CARD_ID), any())).thenReturn(returned);

    mockMvc
        .perform(
            post("/api/keycards/{id}/return", CARD_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lastReturnTime").isNotEmpty());
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void returnKeycard_visitor_returns403() throws Exception {
    ReturnKeycardRequest request = new ReturnKeycardRequest(UUID.randomUUID());

    mockMvc
        .perform(
            post("/api/keycards/{id}/return", CARD_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void returnKeycard_notAssigned_returns409() throws Exception {
    ReturnKeycardRequest request = new ReturnKeycardRequest(UUID.randomUUID());
    when(keycardService.returnKeycard(eq(CARD_ID), any()))
        .thenThrow(
            new BusinessException(
                "Keycard is not currently assigned",
                ErrorCode.BUSINESS_RULE_VIOLATION,
                HttpStatus.CONFLICT));

    mockMvc
        .perform(
            post("/api/keycards/{id}/return", CARD_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser(roles = "RECEPTIONIST")
  void returnKeycard_missingBody_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/keycards/{id}/return", CARD_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }
}
