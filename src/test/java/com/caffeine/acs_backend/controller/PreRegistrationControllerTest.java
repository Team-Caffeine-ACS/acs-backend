package com.caffeine.acs_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.caffeine.acs_backend.dto.preregistration.*;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.service.PreRegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // UUS IMPORT
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PreRegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JacksonAutoConfiguration.class)
class PreRegistrationControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  // ASENDUS: @MockBean -> @MockitoBean
  @MockitoBean private PreRegistrationService preRegistrationService;

  @MockitoBean private com.caffeine.acs_backend.security.JwtService jwtService;

  @MockitoBean private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

  @MockitoBean
  private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

  @Test
  void create_returns201() throws Exception {
    CreatePreRegistrationRequest request =
        new CreatePreRegistrationRequest(
            UUID.randomUUID(), // personId
            LocalDateTime.now(), // expectedArrival
            UUID.randomUUID(), // hostId
            "Test notes", // notes
            UUID.randomUUID());

    when(preRegistrationService.create(any()))
        .thenReturn(org.mockito.Mockito.mock(CreatePreRegistrationResponse.class));

    mockMvc
        .perform(
            post("/api/pre-registrations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    verify(preRegistrationService).create(any());
  }

  @Test
  void getById_returns200() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc.perform(get("/api/pre-registrations/{id}", id)).andExpect(status().isOk());

    verify(preRegistrationService).getById(id);
  }

  @Test
  void getAll_returns200() throws Exception {
    mockMvc
        .perform(get("/api/pre-registrations").param("search", "test").param("status", "PLANNED"))
        .andExpect(status().isOk());

    verify(preRegistrationService)
        .getAll(any(), eq("test"), eq(VisitStatus.PLANNED), any(), any(Pageable.class));
  }

  @Test
  void update_returns200() throws Exception {
    UUID id = UUID.randomUUID();
    UpdatePreRegistrationRequest request =
        new UpdatePreRegistrationRequest(
            LocalDateTime.now().plusDays(1), // uus aeg
            "Uuendatud märkmed",
            UUID.randomUUID() // märkmed
            );

    mockMvc
        .perform(
            put("/api/pre-registrations/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    verify(preRegistrationService).update(eq(id), any());
  }

  @Test
  void cancel_returns204() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/pre-registrations/{id}", id).param("message", "Cancelled by user"))
        .andExpect(status().isNoContent());

    verify(preRegistrationService).cancel(id, "Cancelled by user");
  }

  @Test
  void notify_returns204() throws Exception {
    UUID id = UUID.randomUUID();
    NotifyRequest request = new NotifyRequest("Test notification");

    mockMvc
        .perform(
            post("/api/pre-registrations/{id}/notify", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    verify(preRegistrationService).resendNotification(eq(id), any());
  }
}
