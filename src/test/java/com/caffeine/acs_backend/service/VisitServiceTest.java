package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.caffeine.acs_backend.dto.visit.CreateVisitRequest;
import com.caffeine.acs_backend.dto.visit.EditVisitRequest;
import com.caffeine.acs_backend.dto.visit.ExitVisitRequest;
import com.caffeine.acs_backend.dto.visit.VisitDetailResponse;
import com.caffeine.acs_backend.dto.visit.VisitTimelineEntry;
import com.caffeine.acs_backend.entity.*;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

  @Mock private AccessPointRepository accessPointRepository;
  @Mock private PersonInRoleRepository personInRoleRepository;
  @Mock private KeycardRepository keycardRepository;
  @Mock private KeycardInPossessionRepository keycardInPossessionRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private PersonRepository personRepository;
  @Mock private VisitRepository visitRepository;

  @InjectMocks private VisitService visitService;

  // ── Helpers ──────────────────────────────────────────────────────────────────

  private Person person(String givenName, String surname) {
    Person p = new Person();
    p.setGivenName(givenName);
    p.setSurname(surname);
    return p;
  }

  private PersonInRole personInRole(String givenName, String surname) {
    PersonInRole pir = new PersonInRole();
    pir.setPerson(person(givenName, surname));
    return pir;
  }

  private Visit visitWithVisitor(PersonInRole visitor) {
    return Visit.builder()
        .arrivalTime(LocalDateTime.now().minusHours(1))
        .accessPoint(new AccessPoint())
        .visitor(visitor)
        .assignor(personInRole("Assignor", "User"))
        .build();
  }

  // ── getVisit ─────────────────────────────────────────────────────────────────

  @Test
  void getVisit_notFound_throwsBusinessException() {
    UUID id = UUID.randomUUID();
    when(visitRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> visitService.getVisit(id))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void getVisit_found_noHost_noCard_returnsDetail() {
    UUID id = UUID.randomUUID();
    PersonInRole visitor = personInRole("John", "Smith");
    Visit visit = visitWithVisitor(visitor);
    visit.setId(id);
    visit.setComment("Test visit");

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
    when(keycardInPossessionRepository.findActiveByHolder(visitor)).thenReturn(Optional.empty());

    VisitDetailResponse response = visitService.getVisit(id);

    assertThat(response.firstName()).isEqualTo("John");
    assertThat(response.lastName()).isEqualTo("Smith");
    assertThat(response.hostName()).isNull();
    assertThat(response.cardId()).isNull();
    assertThat(response.visitReason()).isEqualTo("Test visit");
  }

  @Test
  void getVisit_withHost_includesHostName() {
    UUID id = UUID.randomUUID();
    PersonInRole visitor = personInRole("Jane", "Doe");
    PersonInRole host = personInRole("Host", "Person");
    Visit visit = visitWithVisitor(visitor);
    visit.setId(id);
    visit.setHost(host);

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
    when(keycardInPossessionRepository.findActiveByHolder(visitor)).thenReturn(Optional.empty());

    VisitDetailResponse response = visitService.getVisit(id);

    assertThat(response.hostName()).isEqualTo("Host Person");
  }

  @Test
  void getVisit_withActiveCard_returnsCardId() {
    UUID id = UUID.randomUUID();
    UUID cardId = UUID.randomUUID();
    PersonInRole visitor = personInRole("John", "Smith");
    Visit visit = visitWithVisitor(visitor);
    visit.setId(id);

    Keycard keycard = new Keycard();
    keycard.setId(cardId);
    KeycardInPossession possession =
        KeycardInPossession.builder().keycard(keycard).keycardHolder(visitor).build();

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
    when(keycardInPossessionRepository.findActiveByHolder(visitor))
        .thenReturn(Optional.of(possession));

    VisitDetailResponse response = visitService.getVisit(id);

    assertThat(response.cardId()).isEqualTo(cardId);
  }

  // ── exitVisit ────────────────────────────────────────────────────────────────

  @Test
  void exitVisit_notFound_throwsBusinessException() {
    UUID id = UUID.randomUUID();
    ExitVisitRequest request = new ExitVisitRequest(LocalDateTime.now());
    when(visitRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> visitService.exitVisit(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void exitVisit_alreadyExited_throwsConflict() {
    // 1. Ettevalmistus
    UUID id = UUID.randomUUID();
    PersonInRole visitor = personInRole("John", "Smith");
    Visit visit = visitWithVisitor(visitor);
    visit.setExitTime(LocalDateTime.now().minusMinutes(30));

    ExitVisitRequest request = new ExitVisitRequest(LocalDateTime.now());

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));

    assertThatThrownBy(() -> visitService.exitVisit(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

    verify(visitRepository, never()).save(any());
  }

  @Test
  void exitVisit_success_setsExitTimeAndSaves() {
    UUID id = UUID.randomUUID();
    PersonInRole visitor = personInRole("John", "Smith");
    Visit visit = visitWithVisitor(visitor);
    visit.setId(id);
    LocalDateTime exitTime = LocalDateTime.now();

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
    when(visitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(keycardInPossessionRepository.findActiveByHolder(visitor)).thenReturn(Optional.empty());

    visitService.exitVisit(id, new ExitVisitRequest(exitTime));

    assertThat(visit.getExitTime()).isEqualTo(exitTime);
    verify(visitRepository).save(visit);
  }

  // ── editVisit ────────────────────────────────────────────────────────────────

  @Test
  void editVisit_notFound_throwsBusinessException() {
    UUID id = UUID.randomUUID();
    when(visitRepository.findById(id)).thenReturn(Optional.empty());

    EditVisitRequest request =
        new EditVisitRequest(
            null,
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            null);

    assertThatThrownBy(() -> visitService.editVisit(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void editVisit_assignorNotFound_throwsNotFound() {
    UUID id = UUID.randomUUID();
    UUID assignorId = UUID.randomUUID();
    PersonInRole visitor = personInRole("John", "Smith");
    Visit visit = visitWithVisitor(visitor);

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
    when(personInRoleRepository.findById(assignorId)).thenReturn(Optional.empty());

    EditVisitRequest request =
        new EditVisitRequest(
            null, assignorId, UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), null);

    assertThatThrownBy(() -> visitService.editVisit(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void editVisit_inactiveAssignor_throwsNotFound() {
    UUID id = UUID.randomUUID();
    UUID assignorId = UUID.randomUUID();
    PersonInRole visitor = personInRole("John", "Smith");
    PersonInRole inactiveAssignor = personInRole("Inactive", "Assignor");
    inactiveAssignor.setActive(false);
    Visit visit = visitWithVisitor(visitor);

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
    when(personInRoleRepository.findById(assignorId)).thenReturn(Optional.of(inactiveAssignor));

    EditVisitRequest request =
        new EditVisitRequest(
            null, assignorId, UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), null);

    assertThatThrownBy(() -> visitService.editVisit(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> {
              BusinessException businessException = (BusinessException) ex;
              assertThat(businessException.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
              assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
            });
  }

  @Test
  void editVisit_accessPointNotFound_throwsNotFound() {
    UUID id = UUID.randomUUID();
    UUID assignorId = UUID.randomUUID();
    UUID apId = UUID.randomUUID();
    PersonInRole visitor = personInRole("John", "Smith");
    Visit visit = visitWithVisitor(visitor);

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
    when(personInRoleRepository.findById(assignorId))
        .thenReturn(Optional.of(personInRole("Assignor", "User")));
    when(accessPointRepository.findById(apId)).thenReturn(Optional.empty());

    EditVisitRequest request =
        new EditVisitRequest(
            null, assignorId, apId, LocalDateTime.now(), LocalDateTime.now(), null);

    assertThatThrownBy(() -> visitService.editVisit(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void editVisit_nullHost_clearsExistingHost() {
    UUID id = UUID.randomUUID();
    UUID assignorId = UUID.randomUUID();
    UUID apId = UUID.randomUUID();
    PersonInRole visitor = personInRole("John", "Smith");
    Visit visit = visitWithVisitor(visitor);
    visit.setId(id);
    visit.setHost(personInRole("Old", "Host"));

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
    when(personInRoleRepository.findById(assignorId))
        .thenReturn(Optional.of(personInRole("Assignor", "User")));
    when(accessPointRepository.findById(apId)).thenReturn(Optional.of(new AccessPoint()));
    when(visitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(keycardInPossessionRepository.findActiveByHolder(visitor)).thenReturn(Optional.empty());

    visitService.editVisit(
        id,
        new EditVisitRequest(
            null, assignorId, apId, LocalDateTime.now(), LocalDateTime.now(), null));

    assertThat(visit.getHost()).isNull();
    assertThat(visit.getAssignor()).isNotNull();
    assertThat(visit.getAccessPoint()).isNotNull();
    verify(visitRepository).save(visit);
  }

  @Test
  void editVisit_withNewHost_setsHost() {
    UUID id = UUID.randomUUID();
    UUID assignorId = UUID.randomUUID();
    UUID apId = UUID.randomUUID();
    UUID hostPersonId = UUID.randomUUID();
    PersonInRole visitor = personInRole("John", "Smith");
    PersonInRole newHost = personInRole("New", "Host");
    Person hostPerson = person("New", "Host");
    Visit visit = visitWithVisitor(visitor);
    visit.setId(id);

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
    when(personInRoleRepository.findById(assignorId))
        .thenReturn(Optional.of(personInRole("Assignor", "User")));
    when(accessPointRepository.findById(apId)).thenReturn(Optional.of(new AccessPoint()));
    when(personRepository.findById(hostPersonId)).thenReturn(Optional.of(hostPerson));
    when(personInRoleRepository.findFirstByPersonAndIsActiveTrue(hostPerson))
        .thenReturn(Optional.of(newHost));
    when(visitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(keycardInPossessionRepository.findActiveByHolder(visitor)).thenReturn(Optional.empty());

    VisitDetailResponse response =
        visitService.editVisit(
            id,
            new EditVisitRequest(
                hostPersonId,
                assignorId,
                apId,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Updated"));

    assertThat(visit.getHost()).isEqualTo(newHost);
    assertThat(visit.getComment()).isEqualTo("Updated");
    assertThat(response.hostName()).isEqualTo("New Host");
    assertThat(response.visitReason()).isEqualTo("Updated");
  }

  @Test
  void editVisit_hostPersonNotFound_throwsNotFound() {
    UUID id = UUID.randomUUID();
    UUID assignorId = UUID.randomUUID();
    UUID apId = UUID.randomUUID();
    UUID hostPersonId = UUID.randomUUID();
    PersonInRole visitor = personInRole("John", "Smith");
    Visit visit = visitWithVisitor(visitor);

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
    when(personInRoleRepository.findById(assignorId))
        .thenReturn(Optional.of(personInRole("Assignor", "User")));
    when(accessPointRepository.findById(apId)).thenReturn(Optional.of(new AccessPoint()));
    when(personRepository.findById(hostPersonId)).thenReturn(Optional.empty());
    EditVisitRequest request =
        new EditVisitRequest(
            hostPersonId, assignorId, apId, LocalDateTime.now(), LocalDateTime.now(), "Updated");

    assertThatThrownBy(() -> visitService.editVisit(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void editVisit_hostWithoutActiveRole_throwsNotFound() {
    UUID id = UUID.randomUUID();
    UUID assignorId = UUID.randomUUID();
    UUID apId = UUID.randomUUID();
    UUID hostPersonId = UUID.randomUUID();
    PersonInRole visitor = personInRole("John", "Smith");
    Person hostPerson = person("New", "Host");
    Visit visit = visitWithVisitor(visitor);

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
    when(personInRoleRepository.findById(assignorId))
        .thenReturn(Optional.of(personInRole("Assignor", "User")));
    when(accessPointRepository.findById(apId)).thenReturn(Optional.of(new AccessPoint()));
    when(personRepository.findById(hostPersonId)).thenReturn(Optional.of(hostPerson));
    when(personInRoleRepository.findFirstByPersonAndIsActiveTrue(hostPerson))
        .thenReturn(Optional.empty());
    EditVisitRequest request =
        new EditVisitRequest(
            hostPersonId, assignorId, apId, LocalDateTime.now(), LocalDateTime.now(), "Updated");

    assertThatThrownBy(() -> visitService.editVisit(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void editVisit_updatesArrivalTime() {
    UUID id = UUID.randomUUID();
    UUID assignorId = UUID.randomUUID();
    UUID apId = UUID.randomUUID();
    PersonInRole visitor = personInRole("John", "Smith");
    Visit visit = visitWithVisitor(visitor);
    visit.setId(id);
    LocalDateTime newArrival = LocalDateTime.now().minusDays(1);
    PersonInRole assignor = personInRole("Assignor", "User");
    AccessPoint accessPoint = new AccessPoint();

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
    when(personInRoleRepository.findById(assignorId)).thenReturn(Optional.of(assignor));
    when(accessPointRepository.findById(apId)).thenReturn(Optional.of(accessPoint));
    when(visitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(keycardInPossessionRepository.findActiveByHolder(visitor)).thenReturn(Optional.empty());

    visitService.editVisit(
        id, new EditVisitRequest(null, assignorId, apId, newArrival, newArrival, null));

    assertThat(visit.getArrivalTime()).isEqualTo(newArrival);
    assertThat(visit.getAssignor()).isEqualTo(assignor);
    assertThat(visit.getAccessPoint()).isEqualTo(accessPoint);
  }

  @Test
  void editVisit_entryTimeAfterExitTime_throwsConflict() {
    UUID id = UUID.randomUUID();
    UUID assignorId = UUID.randomUUID();
    UUID apId = UUID.randomUUID();
    PersonInRole visitor = personInRole("John", "Smith");
    Visit visit = visitWithVisitor(visitor);
    LocalDateTime exitTime = LocalDateTime.now().minusMinutes(15);
    LocalDateTime newArrival = LocalDateTime.now();
    visit.setExitTime(exitTime);
    EditVisitRequest request =
        new EditVisitRequest(
            null, assignorId, apId, newArrival, exitTime.plusMinutes(1), "Updated");

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));

    assertThatThrownBy(() -> visitService.editVisit(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> {
              BusinessException businessException = (BusinessException) ex;
              assertThat(businessException.getStatus()).isEqualTo(HttpStatus.CONFLICT);
              assertThat(businessException.getErrorCode())
                  .isEqualTo(ErrorCode.BUSINESS_RULE_VIOLATION);
            });

    verify(personInRoleRepository, never()).findById(any());
    verify(accessPointRepository, never()).findById(any());
    verify(visitRepository, never()).save(any());
  }

  // ── getTimeline ───────────────────────────────────────────────────────────────

  @Test
  void getTimeline_notFound_throwsBusinessException() {
    UUID id = UUID.randomUUID();
    when(visitRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> visitService.getTimeline(id))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void getTimeline_arrivalOnly_returnsOneEntry() {
    UUID id = UUID.randomUUID();
    LocalDateTime arrival = LocalDateTime.now().minusHours(2);
    PersonInRole visitor = personInRole("John", "Smith");
    Visit visit = visitWithVisitor(visitor);
    visit.setId(id);
    visit.setArrivalTime(arrival);
    visit.setComment("Security meeting");

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));

    List<VisitTimelineEntry> entries = visitService.getTimeline(id);

    assertThat(entries).hasSize(1);
    assertThat(entries.get(0).eventType()).isEqualTo("ARRIVAL_REGISTERED");
    assertThat(entries.get(0).timestamp()).isEqualTo(arrival);
    assertThat(entries.get(0).details()).isEqualTo("Security meeting");
  }

  @Test
  void getTimeline_withDeparture_returnsTwoEntries() {
    UUID id = UUID.randomUUID();
    LocalDateTime arrival = LocalDateTime.now().minusHours(2);
    LocalDateTime exit = LocalDateTime.now().minusMinutes(30);
    PersonInRole visitor = personInRole("John", "Smith");
    Visit visit = visitWithVisitor(visitor);
    visit.setId(id);
    visit.setArrivalTime(arrival);
    visit.setExitTime(exit);

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));

    List<VisitTimelineEntry> entries = visitService.getTimeline(id);

    assertThat(entries).hasSize(2);
    assertThat(entries.get(0).eventType()).isEqualTo("ARRIVAL_REGISTERED");
    assertThat(entries.get(1).eventType()).isEqualTo("DEPARTURE_REGISTERED");
    assertThat(entries.get(1).timestamp()).isEqualTo(exit);
    assertThat(entries.get(1).details()).isNull();
  }

  // ── createVisit ──────────────────────────────────────────────────────────────

  @Test
  void createVisit_success_withKeycardAndHost() {
    // 1. GIVEN
    UUID personId = UUID.randomUUID();
    UUID apId = UUID.randomUUID();
    UUID keycardId = UUID.randomUUID();
    UUID hostId = UUID.randomUUID();

    User assignorUser = new User();
    Person assignorPerson = person("Assignor", "Boss");
    assignorUser.setPerson(assignorPerson);

    Person visitorPerson = person("Alice", "Wonderland");
    AccessPoint ap = new AccessPoint();
    Keycard card = new Keycard();
    card.setActive(true);
    card.setKeycardNumber("CARD-123");

    Role visitorRole = new Role();
    visitorRole.setName("Visitor");

    PersonInRole hostPIR = personInRole("Host", "User");
    PersonInRole assignorPIR = personInRole("Assignor", "Boss");

    CreateVisitRequest req =
        new CreateVisitRequest(personId, apId, keycardId, hostId, "Meeting", LocalDateTime.now());

    // Mockime kõik vajalikud repositooriumid
    when(personRepository.findById(personId)).thenReturn(Optional.of(visitorPerson));
    when(accessPointRepository.findById(apId)).thenReturn(Optional.of(ap));
    when(keycardRepository.findById(keycardId)).thenReturn(Optional.of(card));
    when(keycardInPossessionRepository.existsByKeycardAndReturnTimeIsNull(card)).thenReturn(false);
    when(personInRoleRepository.findFirstByPersonAndIsActiveTrue(assignorPerson))
        .thenReturn(Optional.of(assignorPIR));
    when(personInRoleRepository.findById(hostId)).thenReturn(Optional.of(hostPIR));
    when(roleRepository.findByName("Visitor")).thenReturn(Optional.of(visitorRole));
    when(personInRoleRepository.findByPersonAndRoleAndIsActiveTrue(eq(visitorPerson), any()))
        .thenReturn(Optional.empty());
    when(personInRoleRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(visitRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    // 2. WHEN
    var response = visitService.createVisit(req, assignorUser);

    // 3. THEN
    assertThat(response).isNotNull();
    assertThat(response.firstName()).isEqualTo("Alice");
    verify(visitRepository).save(any());
    verify(keycardInPossessionRepository).save(any());
  }

  @Test
  void createVisit_keycardInUse_throwsException() {
    // 1. GIVEN
    UUID personId = UUID.randomUUID();
    UUID cardId = UUID.randomUUID();

    // Loome korrektse kiipkaardi
    Keycard card = new Keycard();
    card.setActive(true);
    card.setKeycardNumber("999");

    // LOO KASUTAJA JA SEO SEE ISIKUGA (See on puuduv osa!)
    User assignorUser = new User();
    Person assignorPerson = new Person();
    assignorUser.setPerson(assignorPerson);

    // Mockime vajalikud andmed
    when(personRepository.findById(any())).thenReturn(Optional.of(new Person()));
    when(accessPointRepository.findById(any())).thenReturn(Optional.of(new AccessPoint()));
    when(keycardRepository.findById(cardId)).thenReturn(Optional.of(card));

    // Simuleerime, et kaart on juba kellegi käes
    when(keycardInPossessionRepository.existsByKeycardAndReturnTimeIsNull(card)).thenReturn(true);

    // Mockime ka assignori leidmise, et resolveAssignorFromUser läbi läheks
    lenient()
        .when(personInRoleRepository.findFirstByPersonAndIsActiveTrue(assignorPerson))
        .thenReturn(Optional.of(new PersonInRole()));

    CreateVisitRequest req =
        new CreateVisitRequest(personId, UUID.randomUUID(), cardId, null, "Meeting", null);

    // 2. WHEN & THEN
    assertThatThrownBy(() -> visitService.createVisit(req, assignorUser))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already assigned");
  }

  @Test
  void getVisits_callsRepositoryWithCorrectParams() {
    Pageable pageable = PageRequest.of(0, 10);
    when(visitRepository.findAllFiltered(any(), any(), any(), any(), any(), any()))
        .thenReturn(Page.empty());

    visitService.getVisits("  search  ", " PLANNED ", null, null, null, pageable);

    // Kontrollime, et search ja status on trimmitud ja status on lowercase
    verify(visitRepository)
        .findAllFiltered(eq("search"), eq("planned"), any(), any(), any(), any());
  }

  @Test
  void createVisit_assignorUserHasNoPerson_throwsException() {
    User userWithoutPerson = new User(); // person is null
    CreateVisitRequest req =
        new CreateVisitRequest(UUID.randomUUID(), UUID.randomUUID(), null, null, null, null);

    when(personRepository.findById(any())).thenReturn(Optional.of(new Person()));
    when(accessPointRepository.findById(any())).thenReturn(Optional.of(new AccessPoint()));

    assertThatThrownBy(() -> visitService.createVisit(req, userWithoutPerson))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("no linked Person");
  }
}
