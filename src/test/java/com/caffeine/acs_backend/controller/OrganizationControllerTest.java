package com.caffeine.acs_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.dto.organization.OrganizationResponse;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.security.JwtAccessDeniedHandler;
import com.caffeine.acs_backend.security.JwtAuthFilter;
import com.caffeine.acs_backend.security.JwtAuthenticationEntryPoint;
import com.caffeine.acs_backend.security.JwtService;
import com.caffeine.acs_backend.security.SecurityConfig;
import com.caffeine.acs_backend.service.OrganizationService;
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

@WebMvcTest(OrganizationController.class)
@Import({
  SecurityConfig.class,
  JwtAuthFilter.class,
  JwtAccessDeniedHandler.class,
  JwtAuthenticationEntryPoint.class
})
class OrganizationControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private OrganizationService organizationService;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private UserDetailsService userDetailsService;

  private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private OrganizationResponse sample(String name) {
    return new OrganizationResponse(ID, name);
  }

  // ── GET /api/organizations ────────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void getAll_admin_returns200WithList() throws Exception {
    when(organizationService.getAll()).thenReturn(List.of(sample("Acme Corp"), sample("Beta LLC")));

    mockMvc
        .perform(get("/api/organizations"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Acme Corp"));
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void getAll_nonAdmin_returns403() throws Exception {
    mockMvc.perform(get("/api/organizations")).andExpect(status().isForbidden());
  }

  @Test
  void getAll_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/organizations")).andExpect(status().isUnauthorized());
  }

  // ── POST /api/organizations ───────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void create_admin_returns201() throws Exception {
    when(organizationService.create(any())).thenReturn(sample("Acme Corp"));

    mockMvc
        .perform(
            post("/api/organizations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LookupRequest("Acme Corp"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Acme Corp"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void create_blankName_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/organizations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LookupRequest(""))))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void create_nonAdmin_returns403() throws Exception {
    mockMvc
        .perform(
            post("/api/organizations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LookupRequest("Acme Corp"))))
        .andExpect(status().isForbidden());
  }

  // ── PUT /api/organizations/{id} ───────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void update_admin_returns200() throws Exception {
    when(organizationService.update(eq(ID), any())).thenReturn(sample("Updated"));

    mockMvc
        .perform(
            put("/api/organizations/{id}", ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LookupRequest("Updated"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void update_notFound_returns404() throws Exception {
    when(organizationService.update(eq(ID), any()))
        .thenThrow(
            new BusinessException(
                "Organization not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));

    mockMvc
        .perform(
            put("/api/organizations/{id}", ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LookupRequest("X"))))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void update_blankName_returns400() throws Exception {
    mockMvc
        .perform(
            put("/api/organizations/{id}", ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LookupRequest("   "))))
        .andExpect(status().isBadRequest());
  }

  // ── DELETE /api/organizations/{id} ────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void delete_admin_returns204() throws Exception {
    mockMvc
        .perform(delete("/api/organizations/{id}", ID).with(csrf()))
        .andExpect(status().isNoContent());

    verify(organizationService).delete(ID);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void delete_notFound_returns404() throws Exception {
    doThrow(
            new BusinessException(
                "Organization not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND))
        .when(organizationService)
        .delete(ID);

    mockMvc
        .perform(delete("/api/organizations/{id}", ID).with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void delete_nonAdmin_returns403() throws Exception {
    mockMvc
        .perform(delete("/api/organizations/{id}", ID).with(csrf()))
        .andExpect(status().isForbidden());
  }
}
