package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.caffeine.acs_backend.dto.person.CreatePersonRequest;
import com.caffeine.acs_backend.dto.person.PersonInRoleResponse;
import com.caffeine.acs_backend.dto.person.PersonResponse;
import com.caffeine.acs_backend.entity.DocumentType;
import com.caffeine.acs_backend.entity.Person;
import com.caffeine.acs_backend.entity.PersonInRole;
import com.caffeine.acs_backend.entity.Role;
import com.caffeine.acs_backend.repository.DocumentTypeRepository;
import com.caffeine.acs_backend.repository.PersonInRoleRepository;
import com.caffeine.acs_backend.repository.PersonRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

  @Mock private PersonInRoleRepository personInRoleRepository;
  @Mock private PersonRepository personRepository;
  @Mock private DocumentTypeRepository documentTypeRepository;

  @InjectMocks private PersonService personService;

  // ── createPerson ────────────────────────────────────────────────────────────

  @Test
  void createPerson_basicInfo_savesWithTrimmedNames() {
    // GIVEN
    CreatePersonRequest request =
        new CreatePersonRequest("  Jaan  ", "  Tamm  ", "  jaan@tamm.ee  ", null, null);

    when(personRepository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

    // ACT
    PersonResponse response = personService.createPerson(request);

    // ASSERT
    assertThat(response.givenName()).isEqualTo("Jaan");
    assertThat(response.surname()).isEqualTo("Tamm");
    verify(personRepository, atLeastOnce()).save(any(Person.class));
  }

  @Test
  void createPerson_onlyDocNumberProvided_throwsException() {
    CreatePersonRequest request =
        new CreatePersonRequest(
            "Jaan",
            "Tamm",
            (String) null,
            (java.util.UUID) null,
            "AB123456"); // documentTypeId puudu

    assertThatThrownBy(() -> personService.createPerson(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("provided together");
  }

  @Test
  void createPerson_onlyDocTypeProvided_throwsException() {
    CreatePersonRequest request =
        new CreatePersonRequest(
            "Jaan", "Tamm", null, UUID.randomUUID(), null); // documentNumber puudu

    assertThatThrownBy(() -> personService.createPerson(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("provided together");
  }

  @Test
  void createPerson_docTypeNotFound_throwsException() {
    UUID docTypeId = UUID.randomUUID();
    CreatePersonRequest request =
        new CreatePersonRequest("Jaan", "Tamm", null, docTypeId, "AB123456");

    when(documentTypeRepository.findById(docTypeId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> personService.createPerson(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Document type not found");
  }

  @Test
  void createPerson_withDocument_savesPersonAndDocument() {
    // GIVEN
    UUID docTypeId = UUID.randomUUID();
    CreatePersonRequest request =
        new CreatePersonRequest("Jaan", "Tamm", null, docTypeId, "AB123456");

    DocumentType docType = new DocumentType();
    docType.setName("ID-kaart");

    when(documentTypeRepository.findById(docTypeId)).thenReturn(Optional.of(docType));
    // Mockime save'i, et see tagastaks isiku ja initsialiseeriks listi kui vaja
    when(personRepository.save(any(Person.class)))
        .thenAnswer(
            i -> {
              Person p = i.getArgument(0);
              if (p.getDocuments() == null) p.setDocuments(new ArrayList<>());
              return p;
            });

    // ACT
    PersonResponse response = personService.createPerson(request);

    // ASSERT
    verify(personRepository, times(2))
        .save(any(Person.class)); // Salvestab alguses ja pärast dokumendi lisamist
    assertThat(response.givenName()).isEqualTo("Jaan");
  }

  // ── search ──────────────────────────────────────────────────────────────────

  @Test
  void search_nullOrBlankQuery_returnsEmptyList() {
    assertThat(personService.search(null, "Visitor")).isEmpty();
    assertThat(personService.search("  ", "Visitor")).isEmpty();
    verifyNoInteractions(personInRoleRepository);
  }

  @Test
  void search_validParams_callsRepoAndMapsResponse() {
    // GIVEN
    String query = " Jaan ";
    String roleName = " Visitor ";

    // 1. Loo isik ja anna talle ID ning nimi
    Person p = new Person();
    p.setId(UUID.randomUUID()); // Vajalik pir.getPerson().getId() jaoks
    p.setGivenName("Jaan");
    p.setSurname("Tamm");
    p.setEmail("jaan@tamm.ee");

    // 2. Loo roll ja anna sellele nimi
    Role role = new Role();
    role.setName("Visitor"); // Vajalik pir.getRole().getName() jaoks

    // 3. Pane need kokku PersonInRole objektiks
    PersonInRole pir = new PersonInRole();
    pir.setId(UUID.randomUUID());
    pir.setPerson(p);
    pir.setRole(role); // ÄRA UNUSTA SEDA!

    when(personInRoleRepository.searchByPersonNameAndRole("Jaan", "Visitor"))
        .thenReturn(List.of(pir));

    // ACT
    List<PersonInRoleResponse> results = personService.search(query, roleName);

    // ASSERT
    assertThat(results).hasSize(1);
    assertThat(results.get(0).givenName()).isEqualTo("Jaan");
    assertThat(results.get(0).surname()).isEqualTo("Tamm");
    assertThat(results.get(0).roleName()).isEqualTo("Visitor"); // Nüüd on see olemas!

    String fullResponseName = results.get(0).givenName() + " " + results.get(0).surname();
    assertThat(fullResponseName).isEqualTo("Jaan Tamm");

    verify(personInRoleRepository).searchByPersonNameAndRole("Jaan", "Visitor");
  }
}
