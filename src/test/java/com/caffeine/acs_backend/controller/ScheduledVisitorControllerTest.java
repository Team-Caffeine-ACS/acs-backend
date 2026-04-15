package com.caffeine.acs_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.service.ScheduledVisitorService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScheduledVisitorController.class)
class ScheduledVisitorControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ScheduledVisitorService service;

  @Test
  void shouldReturnAllVisitors() throws Exception {
    when(service.getAll(any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

    mockMvc.perform(get("/scheduled-visitors")).andExpect(status().isOk());
  }

  @Test
  void shouldReturnStats() throws Exception {
    when(service.getStats())
        .thenReturn(
            Map.of(
                "todayVisitors", 10L,
                "activeVisits", 5L,
                "issuedCards", 5L,
                "deniedEntries", 1L));

    mockMvc
        .perform(get("/scheduled-visitors/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.todayVisitors").value(10));
  }

  @Test
  void shouldGetById() throws Exception {
    UUID id = UUID.randomUUID();

    when(service.getById(id))
        .thenReturn(
            new com.caffeine.acs_backend.dto.visitor.ScheduledVisitorResponse(
                id, null, "John Doe", null, "Host", VisitStatus.PRE_REGISTERED));

    mockMvc
        .perform(get("/scheduled-visitors/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("John Doe"));
  }

  @Test
  void shouldCreateVisitor() throws Exception {
    UUID id = UUID.randomUUID();

    when(service.create(any())).thenReturn(id);

    mockMvc
        .perform(
            post("/scheduled-visitors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "personId": "00000000-0000-0000-0000-000000000001",
                                  "buildingId": "00000000-0000-0000-0000-000000000002",
                                  "scheduledTime": "2026-04-15T12:00:00"
                                }
                                """))
        .andExpect(status().isOk());
  }

  @Test
  void shouldExport() throws Exception {
    when(service.export(any(), any(), any(), any())).thenReturn(new byte[] {1, 2, 3});

    mockMvc
        .perform(get("/scheduled-visitors/export"))
        .andExpect(status().isOk())
        .andExpect(header().exists("Content-Disposition"));
  }
}
