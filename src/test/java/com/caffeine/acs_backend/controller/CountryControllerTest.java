package com.caffeine.acs_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.caffeine.acs_backend.dto.country.CountryResponse;
import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.security.JwtAccessDeniedHandler;
import com.caffeine.acs_backend.security.JwtAuthFilter;
import com.caffeine.acs_backend.security.JwtAuthenticationEntryPoint;
import com.caffeine.acs_backend.security.JwtService;
import com.caffeine.acs_backend.security.SecurityConfig;
import com.caffeine.acs_backend.service.CountryService;
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

@WebMvcTest(CountryController.class)
@Import({
  SecurityConfig.class,
  JwtAuthFilter.class,
  JwtAccessDeniedHandler.class,
  JwtAuthenticationEntryPoint.class
})
class CountryControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CountryService countryService;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private UserDetailsService userDetailsService;

  private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private CountryResponse sample(String name) {
    return new CountryResponse(ID, name);
  }

  // ── GET /api/countries ────────────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void getAll_admin_returns200WithList() throws Exception {
    when(countryService.getAllCountries()).thenReturn(List.of(sample("Estonia"), sample("Latvia")));

    mockMvc
        .perform(get("/api/countries"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Estonia"));
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void getAll_nonAdmin_returns403() throws Exception {
    mockMvc.perform(get("/api/countries")).andExpect(status().isForbidden());
  }

  @Test
  void getAll_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/countries")).andExpect(status().isUnauthorized());
  }

  // ── POST /api/countries ───────────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void create_admin_returns201() throws Exception {
    when(countryService.create(any())).thenReturn(sample("Estonia"));

    mockMvc
        .perform(
            post("/api/countries")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LookupRequest("Estonia"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Estonia"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void create_blankName_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/countries")
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
            post("/api/countries")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LookupRequest("Estonia"))))
        .andExpect(status().isForbidden());
  }

  // ── PUT /api/countries/{id} ───────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void update_admin_returns200() throws Exception {
    when(countryService.update(eq(ID), any())).thenReturn(sample("Updated"));

    mockMvc
        .perform(
            put("/api/countries/{id}", ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LookupRequest("Updated"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void update_notFound_returns404() throws Exception {
    when(countryService.update(eq(ID), any()))
        .thenThrow(
            new BusinessException(
                "Country not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));

    mockMvc
        .perform(
            put("/api/countries/{id}", ID)
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
            put("/api/countries/{id}", ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LookupRequest("   "))))
        .andExpect(status().isBadRequest());
  }

  // ── DELETE /api/countries/{id} ────────────────────────────────────────────────

  @Test
  @WithMockUser(roles = "ADMIN")
  void delete_admin_returns204() throws Exception {
    mockMvc
        .perform(delete("/api/countries/{id}", ID).with(csrf()))
        .andExpect(status().isNoContent());

    verify(countryService).delete(ID);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void delete_notFound_returns404() throws Exception {
    doThrow(
            new BusinessException(
                "Country not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND))
        .when(countryService)
        .delete(ID);

    mockMvc
        .perform(delete("/api/countries/{id}", ID).with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "VISITOR")
  void delete_nonAdmin_returns403() throws Exception {
    mockMvc
        .perform(delete("/api/countries/{id}", ID).with(csrf()))
        .andExpect(status().isForbidden());
  }
}
