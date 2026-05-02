package com.caffeine.acs_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.caffeine.acs_backend.dto.person.CreatePersonRequest;
import com.caffeine.acs_backend.dto.person.PersonResponse;
import com.caffeine.acs_backend.service.PersonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PersonController.class)
@AutoConfigureMockMvc(addFilters = false)
class PersonControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private PersonService personService;

  // --- SECURITY MOCKID KONTEKSTI LAADIMISEKS ---
  @MockitoBean private com.caffeine.acs_backend.security.JwtService jwtService;

  @MockitoBean private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

  @MockitoBean
  private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

  // --------------------------------------------

  @Test
  void createPerson_returns201() throws Exception {
    // GIVEN
    // Kuna CreatePersonRequest on tõenäoliselt record, täidame kanoonilise konstruktori
    // Asenda väljad vastavalt oma recordi definitsioonile
    CreatePersonRequest request =
        new CreatePersonRequest(
            "Jaan",
            "Tamm",
            "jaan@tamm.ee",
            UUID.randomUUID(), // documentTypeId
            "AB1234567");

    when(personService.createPerson(any()))
        .thenReturn(org.mockito.Mockito.mock(PersonResponse.class));

    // ACT & ASSERT
    mockMvc
        .perform(
            post("/api/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    verify(personService).createPerson(any());
  }

  @Test
  void search_returns200() throws Exception {
    // GIVEN
    String query = "Jane";
    String role = "Employee";
    when(personService.search(query, role)).thenReturn(List.of());

    // ACT & ASSERT
    mockMvc
        .perform(get("/api/persons/search").param("q", query).param("role", role))
        .andExpect(status().isOk());

    verify(personService).search(query, role);
  }

  @Test
  void search_withDefaultRole_returns200() throws Exception {
    // ACT & ASSERT
    mockMvc.perform(get("/api/persons/search").param("q", "Jane")).andExpect(status().isOk());

    // Kontrollime, kas tuli vaikimisi "Visitor", nagu kontrolleris märgitud
    verify(personService).search("Jane", "Visitor");
  }
}
