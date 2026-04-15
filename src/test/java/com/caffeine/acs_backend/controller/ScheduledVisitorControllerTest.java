package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.visitor.ScheduledVisitorResponse;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.service.ScheduledVisitorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = ScheduledVisitorController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class ScheduledVisitorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduledVisitorService service;

    @Test
    void shouldReturnAllVisitors() throws Exception {

        ScheduledVisitorResponse response =
                new ScheduledVisitorResponse(
                        UUID.randomUUID(),
                        LocalDateTime.now(),
                        "John Doe",
                        null,
                        "Host",
                        VisitStatus.PRE_REGISTERED
                );

        when(service.getAll(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/scheduled-visitors"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetById() throws Exception {

        UUID id = UUID.randomUUID();

        ScheduledVisitorResponse response =
                new ScheduledVisitorResponse(
                        id,
                        LocalDateTime.now(),
                        "John Doe",
                        null,
                        "Host",
                        VisitStatus.PRE_REGISTERED
                );

        when(service.getById(id)).thenReturn(response);

        mockMvc.perform(get("/scheduled-visitors/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnStats() throws Exception {

        when(service.getStats()).thenReturn(
                java.util.Map.of(
                        "todayVisitors", 1L,
                        "activeVisits", 1L,
                        "issuedCards", 1L,
                        "deniedEntries", 0L
                )
        );

        mockMvc.perform(get("/scheduled-visitors/stats"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldExport() throws Exception {

        when(service.export(any(), any(), any(), any()))
                .thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/scheduled-visitors/export"))
                .andExpect(status().isOk());
    }
}