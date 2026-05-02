package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.caffeine.acs_backend.dto.preregistration.CreatePreRegistrationRequest;
import com.caffeine.acs_backend.dto.preregistration.CreatePreRegistrationResponse;
import com.caffeine.acs_backend.dto.preregistration.NotifyRequest;
import com.caffeine.acs_backend.dto.preregistration.UpdatePreRegistrationRequest;
import com.caffeine.acs_backend.entity.*;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PreRegistrationServiceTest {

  @Mock private VisitRepository visitRepository;
  @Mock private AccessPointRepository accessPointRepository;
  @Mock private UserRepository userRepository;
  @Mock private EmailService emailService;
  @Mock private PersonInRoleRepository personInRoleRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private PersonRepository personRepository;

  @InjectMocks private PreRegistrationService preRegistrationService;

  @BeforeEach
  void setupSecurityContext() {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    // Lisame lenient(), et vältida UnnecessaryStubbingExceptionit
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    lenient().when(authentication.getName()).thenReturn("admin@test.com");
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void create_FullFlow_Success() {
    UUID personId = UUID.randomUUID();
    UUID buildingId = UUID.randomUUID();
    CreatePreRegistrationRequest request =
        new CreatePreRegistrationRequest(
            personId, LocalDateTime.now().plusDays(1), null, "Note", buildingId);

    User currentUser = new User();
    currentUser.setEmail("admin@test.com");
    Person visitorPerson = new Person();
    visitorPerson.setEmail("visitor@example.com");
    visitorPerson.setGivenName("John");
    visitorPerson.setSurname("Doe");
    AccessPoint building = new AccessPoint();
    building.setName("Main Building");
    Role visitorRole = new Role();
    visitorRole.setName("Visitor");

    when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(currentUser));
    when(accessPointRepository.findById(buildingId)).thenReturn(Optional.of(building));
    when(personRepository.findById(personId)).thenReturn(Optional.of(visitorPerson));
    when(roleRepository.findByName("Visitor")).thenReturn(Optional.of(visitorRole));
    when(personInRoleRepository.findByPersonAndRoleAndIsActiveTrue(any(), any()))
        .thenReturn(Optional.empty());
    when(personInRoleRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(visitRepository.save(any()))
        .thenAnswer(
            inv -> {
              Visit v = inv.getArgument(0);
              v.setId(UUID.randomUUID());
              return v;
            });

    CreatePreRegistrationResponse response = preRegistrationService.create(request);

    assertThat(response).isNotNull();
    verify(emailService)
        .sendVisitorNotification(anyString(), anyString(), anyString(), anyString(), any());
  }

  @Test
  void create_BuildingNotFound_ThrowsBadRequest() {
    UUID buildingId = UUID.randomUUID();
    CreatePreRegistrationRequest request =
        new CreatePreRegistrationRequest(
            UUID.randomUUID(), LocalDateTime.now(), null, "Note", buildingId);

    when(accessPointRepository.findById(buildingId)).thenReturn(Optional.empty());

    ResponseStatusException ex =
        org.junit.jupiter.api.Assertions.assertThrows(
            ResponseStatusException.class, () -> preRegistrationService.create(request));
    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void findPreRegistration_IsExpired_ThrowsGone() {
    UUID id = UUID.randomUUID();
    Visit expiredVisit = new Visit();
    expiredVisit.setStatus(VisitStatus.EXPIRED);

    when(visitRepository.findById(id)).thenReturn(Optional.of(expiredVisit));

    ResponseStatusException ex =
        org.junit.jupiter.api.Assertions.assertThrows(
            ResponseStatusException.class, () -> preRegistrationService.getById(id));
    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.GONE);
  }

  @Test
  void resendNotification_NoEmail_ThrowsBadRequest() {
    UUID id = UUID.randomUUID();
    Visit visit = new Visit();
    visit.setStatus(VisitStatus.PLANNED);
    Person person = new Person(); // email on null
    visit.setVisitor(PersonInRole.builder().person(person).build());

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));

    ResponseStatusException ex =
        org.junit.jupiter.api.Assertions.assertThrows(
            ResponseStatusException.class,
            () -> preRegistrationService.resendNotification(id, null));
    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void getById_Success() {
    UUID id = UUID.randomUUID();
    Visit visit = new Visit();
    visit.setStatus(VisitStatus.PLANNED);

    // Loome ja seostame hoone, et vältida NPE-d
    AccessPoint ap = new AccessPoint();
    ap.setName("Test Building");
    visit.setAccessPoint(ap);

    // Seostame ka külalise, kuna Response.from() võib seda vajada
    Person person = new Person();
    person.setGivenName("John");
    person.setSurname("Doe");
    visit.setVisitor(PersonInRole.builder().person(person).build());

    User currentUser = new User();
    currentUser.setEmail("admin@test.com");

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(currentUser));
    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));

    var response = preRegistrationService.getById(id);

    assertThat(response).isNotNull();
    verify(visitRepository).findById(id);
  }

  @Test
  void cancel_WithEmail_SendsNotification() {
    UUID id = UUID.randomUUID();
    Visit visit = new Visit();
    visit.setStatus(VisitStatus.PLANNED);

    Person person = new Person();
    person.setEmail("visitor@test.com");
    visit.setVisitor(PersonInRole.builder().person(person).build());

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));

    preRegistrationService.cancel(id, "Canceled by admin");

    assertThat(visit.getStatus()).isEqualTo(VisitStatus.EXPIRED);
    verify(emailService)
        .sendCancellationNotification(eq("visitor@test.com"), anyString(), anyString());
    verify(visitRepository).save(visit);
  }

  @Test
  void update_AllFields_Success() {
    UUID id = UUID.randomUUID();
    UUID buildingId = UUID.randomUUID();
    Visit visit = new Visit();
    AccessPoint newBuilding = new AccessPoint();

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
    when(accessPointRepository.findById(buildingId)).thenReturn(Optional.of(newBuilding));
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

    UpdatePreRegistrationRequest request =
        new UpdatePreRegistrationRequest(
            LocalDateTime.now().plusDays(2), "Updated notes", buildingId);

    preRegistrationService.update(id, request);

    assertThat(visit.getNotes()).isEqualTo("Updated notes");
    assertThat(visit.getAccessPoint()).isEqualTo(newBuilding);
    verify(visitRepository).save(visit);
  }

  @Test
  void getAll_WithResults_CoversLambdas() {
    // SETUP
    UUID buildingId = UUID.randomUUID();
    Visit visit = new Visit();
    visit.setStatus(VisitStatus.PLANNED);

    // Täidame objekti, et vältida NPE-sid lambdades (PreRegistrationResponse.from)
    AccessPoint ap = new AccessPoint();
    ap.setName("Building A");
    visit.setAccessPoint(ap);
    Person person = new Person();
    person.setGivenName("Jane");
    person.setSurname("Doe");
    visit.setVisitor(PersonInRole.builder().person(person).build());

    Page<Visit> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(visit));

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
    when(visitRepository.findPreRegistrations(any(), any(), any(), any(), any(), any()))
        .thenReturn(page);

    // ACT
    var result =
        preRegistrationService.getAll(
            LocalDate.now(), "search", VisitStatus.PLANNED, buildingId, Pageable.unpaged());

    // ASSERT
    assertThat(result.getContent()).hasSize(1);
    verify(visitRepository).findPreRegistrations(any(), any(), any(), any(), any(), any());
  }

  @Test
  void cancel_NoEmail_DoesNotNotify() {
    UUID id = UUID.randomUUID();
    Visit visit = new Visit();
    visit.setStatus(VisitStatus.PLANNED);
    // Visitor on olemas, aga isikul puudub e-mail
    visit.setVisitor(PersonInRole.builder().person(new Person()).build());

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));

    preRegistrationService.cancel(id, "Reason");

    assertThat(visit.getStatus()).isEqualTo(VisitStatus.EXPIRED);
    verify(emailService, never()).sendCancellationNotification(any(), any(), any());
  }

  @Test
  void update_OnlyNotes_CoversBranches() {
    UUID id = UUID.randomUUID();
    Visit visit = new Visit();
    visit.setStatus(VisitStatus.PLANNED);
    // Vajalik Response.from jaoks
    visit.setAccessPoint(new AccessPoint());
    visit.setVisitor(PersonInRole.builder().person(new Person()).build());

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

    // Ainult märkmed, teised väljad nullid
    UpdatePreRegistrationRequest request =
        new UpdatePreRegistrationRequest(null, "Just notes", null);

    preRegistrationService.update(id, request);

    assertThat(visit.getNotes()).isEqualTo("Just notes");
    verify(accessPointRepository, never()).findById(any()); // buildingId oli null
  }

  @Test
  void create_NoHost_CoversBranch() {
    CreatePreRegistrationRequest request =
        new CreatePreRegistrationRequest(
            UUID.randomUUID(), LocalDateTime.now(), null, "Note", UUID.randomUUID());

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
    when(accessPointRepository.findById(any())).thenReturn(Optional.of(new AccessPoint()));
    when(personRepository.findById(any())).thenReturn(Optional.of(new Person()));
    when(roleRepository.findByName(anyString())).thenReturn(Optional.of(new Role()));
    when(personInRoleRepository.findByPersonAndRoleAndIsActiveTrue(any(), any()))
        .thenReturn(Optional.of(new PersonInRole()));
    when(visitRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    preRegistrationService.create(request);

    // Kontrollime, et hosti otsingut ei toimunud
    verify(personInRoleRepository, times(0)).findById(any(UUID.class));
  }

  @Test
  void resendNotification_Success() {
    UUID id = UUID.randomUUID();
    Visit visit = new Visit();
    visit.setStatus(VisitStatus.PLANNED);
    visit.setArrivalTime(LocalDateTime.now());

    AccessPoint ap = new AccessPoint();
    ap.setName("Main Gate");
    visit.setAccessPoint(ap);

    Person person = new Person();
    person.setEmail("visitor@example.com");
    person.setGivenName("John");
    person.setSurname("Doe");
    visit.setVisitor(PersonInRole.builder().person(person).build());

    when(visitRepository.findById(id)).thenReturn(Optional.of(visit));

    // Testime nii, et request on olemas (lisasõnumiga)
    NotifyRequest request = new NotifyRequest("Please be on time");

    preRegistrationService.resendNotification(id, request);

    verify(emailService)
        .sendVisitorNotification(
            eq("visitor@example.com"),
            anyString(),
            anyString(),
            eq("Main Gate"),
            eq("Please be on time"));
  }

  @Test
  void create_PersonInRoleExists_CoversBranch() {
    UUID personId = UUID.randomUUID();
    CreatePreRegistrationRequest request =
        new CreatePreRegistrationRequest(
            personId, LocalDateTime.now(), null, "Note", UUID.randomUUID());

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
    when(accessPointRepository.findById(any())).thenReturn(Optional.of(new AccessPoint()));
    when(personRepository.findById(personId)).thenReturn(Optional.of(new Person()));
    when(roleRepository.findByName("Visitor")).thenReturn(Optional.of(new Role()));

    // SIHTMÄRK: Tagastame olemasoleva PersonInRole, mitte ei loo uut
    PersonInRole existingPir = new PersonInRole();
    when(personInRoleRepository.findByPersonAndRoleAndIsActiveTrue(any(), any()))
        .thenReturn(Optional.of(existingPir));

    when(visitRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    preRegistrationService.create(request);

    // Kontrollime, et save() EI kutsutud PersonInRole jaoks
    verify(personInRoleRepository, never()).save(any(PersonInRole.class));
  }

  @Test
  void getAll_NoDate_CoversBranch() {
    // Kasutame jälle sisselogitud kasutajat
    User currentUser = new User();
    lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(currentUser));

    // Kasutame meetodi väljakutses konkreetseid null-väärtusi,
    // aga Mockito stub-is võime olla leebemad
    lenient()
        .when(visitRepository.findPreRegistrations(any(), any(), isNull(), isNull(), any(), any()))
        .thenReturn(org.springframework.data.domain.Page.empty());

    // ACT
    preRegistrationService.getAll(null, "search", VisitStatus.PLANNED, null, Pageable.unpaged());

    // ASSERT
    verify(visitRepository).findPreRegistrations(any(), any(), isNull(), isNull(), any(), any());
  }
}
