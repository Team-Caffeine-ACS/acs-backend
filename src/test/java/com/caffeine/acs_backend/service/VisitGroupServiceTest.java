package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.caffeine.acs_backend.dto.visitgroup.CreateGroupVisitRequest;
import com.caffeine.acs_backend.dto.visitgroup.GroupVisitResponse;
import com.caffeine.acs_backend.entity.*;
import com.caffeine.acs_backend.enums.VisitStatus;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class VisitGroupServiceTest {

  @Mock private GroupRepository groupRepository;
  @Mock private GroupInVisitRepository groupInVisitRepository;
  @Mock private VisitRepository visitRepository;
  @Mock private AccessPointRepository accessPointRepository;
  @Mock private PersonRepository personRepository;
  @Mock private PersonInRoleRepository personInRoleRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private EmailService emailService;

  @InjectMocks private VisitGroupService visitGroupService;

  // ── Helpers ──────────────────────────────────────────────────────────────────

  private static final LocalDateTime ARRIVAL = LocalDateTime.of(2026, 5, 2, 10, 0);
  private static final LocalDateTime EXIT = LocalDateTime.of(2026, 5, 2, 18, 0);

  private Person person(String givenName, String surname) {
    Person p = new Person();
    p.setId(UUID.randomUUID());
    p.setGivenName(givenName);
    p.setSurname(surname);
    p.setEmail(givenName.toLowerCase() + "@test.com");
    return p;
  }

  private PersonInRole personInRole(Person person) {
    PersonInRole pir = new PersonInRole();
    pir.setId(UUID.randomUUID());
    pir.setPerson(person);
    pir.setActive(true);
    return pir;
  }

  private Role visitorRole() {
    Role r = new Role();
    r.setId(UUID.randomUUID());
    r.setName("Visitor");
    return r;
  }

  private AccessPoint building() {
    AccessPoint ap = new AccessPoint();
    ap.setId(UUID.randomUUID());
    ap.setName("Main Entrance");
    return ap;
  }

  private Group group(String name) {
    Group g = Group.builder().name(name).description("Test group").build();
    g.setId(UUID.randomUUID());
    return g;
  }

  private GroupInVisit groupInVisit(Group group) {
    GroupInVisit giv =
        GroupInVisit.builder()
            .group(group)
            .plannedArrival(ARRIVAL)
            .plannedExit(EXIT)
            .comment("Test comment")
            .build();
    giv.setId(UUID.randomUUID());
    return giv;
  }

  private Visit visit(PersonInRole visitor, GroupInVisit giv, AccessPoint ap, VisitStatus status) {
    Visit v =
        Visit.builder()
            .arrivalTime(ARRIVAL)
            .accessPoint(ap)
            .visitor(visitor)
            .groupInVisit(giv)
            .status(status)
            .build();
    v.setId(UUID.randomUUID());
    return v;
  }

  private CreateGroupVisitRequest createRequest(UUID buildingId, List<UUID> personIds) {
    return new CreateGroupVisitRequest(
        "Test Group", "Description", personIds, ARRIVAL, EXIT, null, buildingId, "Test comment");
  }

  // ── create ────────────────────────────────────────────────────────────────────

  @Test
  void create_withTwoPersons_returnsResponseWithTwoMembers() {
    AccessPoint ap = building();
    Role role = visitorRole();
    Person p1 = person("Brian", "May");
    Person p2 = person("Roger", "Taylor");
    PersonInRole pir1 = personInRole(p1);
    PersonInRole pir2 = personInRole(p2);
    Group grp = group("Test Group");
    GroupInVisit giv = groupInVisit(grp);

    when(accessPointRepository.findById(ap.getId())).thenReturn(Optional.of(ap));
    when(roleRepository.findByName("Visitor")).thenReturn(Optional.of(role));
    when(groupRepository.findByName("Test Group")).thenReturn(Optional.empty());
    when(groupRepository.save(any())).thenReturn(grp);
    when(groupInVisitRepository.save(any())).thenReturn(giv);
    when(personRepository.findById(p1.getId())).thenReturn(Optional.of(p1));
    when(personRepository.findById(p2.getId())).thenReturn(Optional.of(p2));
    when(personInRoleRepository.findByPersonAndRoleAndIsActiveTrue(p1, role))
        .thenReturn(Optional.of(pir1));
    when(personInRoleRepository.findByPersonAndRoleAndIsActiveTrue(p2, role))
        .thenReturn(Optional.of(pir2));
    when(visitRepository.save(any()))
        .thenAnswer(
            inv -> {
              Visit v = inv.getArgument(0);
              v.setId(UUID.randomUUID());
              return v;
            });

    CreateGroupVisitRequest request = createRequest(ap.getId(), List.of(p1.getId(), p2.getId()));
    GroupVisitResponse response = visitGroupService.create(request);

    assertThat(response.groupName()).isEqualTo("Test Group");
    assertThat(response.memberCount()).isEqualTo(2);
    assertThat(response.members()).hasSize(2);
    assertThat(response.members())
        .extracting("fullName")
        .containsExactlyInAnyOrder("Brian May", "Roger Taylor");
    verify(visitRepository, times(2)).save(any());
  }

  @Test
  void create_reusesExistingGroupByName() {
    AccessPoint ap = building();
    Role role = visitorRole();
    Person p1 = person("Brian", "May");
    PersonInRole pir1 = personInRole(p1);
    Group existingGroup = group("Existing Group");
    GroupInVisit giv = groupInVisit(existingGroup);

    when(accessPointRepository.findById(ap.getId())).thenReturn(Optional.of(ap));
    when(roleRepository.findByName("Visitor")).thenReturn(Optional.of(role));
    when(groupRepository.findByName("Test Group")).thenReturn(Optional.of(existingGroup));
    when(groupInVisitRepository.save(any())).thenReturn(giv);
    when(personRepository.findById(p1.getId())).thenReturn(Optional.of(p1));
    when(personInRoleRepository.findByPersonAndRoleAndIsActiveTrue(p1, role))
        .thenReturn(Optional.of(pir1));
    when(visitRepository.save(any()))
        .thenAnswer(
            inv -> {
              Visit v = inv.getArgument(0);
              v.setId(UUID.randomUUID());
              return v;
            });

    CreateGroupVisitRequest request = createRequest(ap.getId(), List.of(p1.getId()));
    visitGroupService.create(request);

    verify(groupRepository, never()).save(any());
  }

  @Test
  void create_buildingNotFound_throws400() {
    UUID fakeBuildingId = UUID.randomUUID();
    when(accessPointRepository.findById(fakeBuildingId)).thenReturn(Optional.empty());

    CreateGroupVisitRequest request = createRequest(fakeBuildingId, List.of(UUID.randomUUID()));

    assertThatThrownBy(() -> visitGroupService.create(request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode().value()).isEqualTo(400);
            });
  }

  @Test
  void create_personNotFound_throws400() {
    AccessPoint ap = building();
    Role role = visitorRole();
    UUID fakePersonId = UUID.randomUUID();

    when(accessPointRepository.findById(ap.getId())).thenReturn(Optional.of(ap));
    when(roleRepository.findByName("Visitor")).thenReturn(Optional.of(role));
    when(groupRepository.findByName("Test Group")).thenReturn(Optional.empty());
    when(groupRepository.save(any())).thenReturn(group("Test Group"));
    when(groupInVisitRepository.save(any())).thenReturn(groupInVisit(group("Test Group")));
    when(personRepository.findById(fakePersonId)).thenReturn(Optional.empty());

    CreateGroupVisitRequest request = createRequest(ap.getId(), List.of(fakePersonId));

    assertThatThrownBy(() -> visitGroupService.create(request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode().value()).isEqualTo(400);
            });
  }

  @Test
  void create_sendsEmailToEachMember() {
    AccessPoint ap = building();
    Role role = visitorRole();
    Person p1 = person("Brian", "May");
    Person p2 = person("Roger", "Taylor");

    when(accessPointRepository.findById(ap.getId())).thenReturn(Optional.of(ap));
    when(roleRepository.findByName("Visitor")).thenReturn(Optional.of(role));
    when(groupRepository.findByName(any())).thenReturn(Optional.empty());
    when(groupRepository.save(any())).thenReturn(group("Test Group"));
    when(groupInVisitRepository.save(any())).thenReturn(groupInVisit(group("Test Group")));
    when(personRepository.findById(p1.getId())).thenReturn(Optional.of(p1));
    when(personRepository.findById(p2.getId())).thenReturn(Optional.of(p2));
    when(personInRoleRepository.findByPersonAndRoleAndIsActiveTrue(any(), any()))
        .thenAnswer(inv -> Optional.of(personInRole(inv.getArgument(0))));
    when(visitRepository.save(any()))
        .thenAnswer(
            inv -> {
              Visit v = inv.getArgument(0);
              v.setId(UUID.randomUUID());
              return v;
            });

    CreateGroupVisitRequest request = createRequest(ap.getId(), List.of(p1.getId(), p2.getId()));
    visitGroupService.create(request);

    verify(emailService, times(2)).sendVisitorNotification(any(), any(), any(), any(), any());
  }

  // ── getById ───────────────────────────────────────────────────────────────────

  @Test
  void getById_returnsGroupWithMembers() {
    Group grp = group("Queen");
    GroupInVisit giv = groupInVisit(grp);
    AccessPoint ap = building();
    Person p1 = person("Brian", "May");
    Person p2 = person("Roger", "Taylor");
    PersonInRole pir1 = personInRole(p1);
    PersonInRole pir2 = personInRole(p2);
    Visit v1 = visit(pir1, giv, ap, VisitStatus.PLANNED);
    Visit v2 = visit(pir2, giv, ap, VisitStatus.IN_BUILDING);

    when(groupInVisitRepository.findById(giv.getId())).thenReturn(Optional.of(giv));
    when(visitRepository.findByGroupInVisitId(giv.getId())).thenReturn(List.of(v1, v2));

    GroupVisitResponse response = visitGroupService.getById(giv.getId());

    assertThat(response.groupName()).isEqualTo("Queen");
    assertThat(response.memberCount()).isEqualTo(2);
    assertThat(response.checkedInCount()).isEqualTo(1);
    assertThat(response.departedCount()).isZero();
    assertThat(response.building()).isEqualTo("Main Entrance");
  }

  @Test
  void getById_countsDepartedCorrectly() {
    Group grp = group("Queen");
    GroupInVisit giv = groupInVisit(grp);
    AccessPoint ap = building();
    Person p1 = person("Brian", "May");
    Person p2 = person("Roger", "Taylor");
    Visit v1 = visit(personInRole(p1), giv, ap, VisitStatus.IN_BUILDING);
    Visit v2 = visit(personInRole(p2), giv, ap, VisitStatus.DEPARTED);

    when(groupInVisitRepository.findById(giv.getId())).thenReturn(Optional.of(giv));
    when(visitRepository.findByGroupInVisitId(giv.getId())).thenReturn(List.of(v1, v2));

    GroupVisitResponse response = visitGroupService.getById(giv.getId());

    assertThat(response.checkedInCount()).isEqualTo(1);
    assertThat(response.departedCount()).isEqualTo(1);
  }

  @Test
  void getById_notFound_throws404() {
    UUID fakeId = UUID.randomUUID();
    when(groupInVisitRepository.findById(fakeId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> visitGroupService.getById(fakeId))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode().value()).isEqualTo(404);
            });
  }

  @Test
  void getById_noVisitsFound_throws404() {
    Group grp = group("Queen");
    GroupInVisit giv = groupInVisit(grp);
    UUID givId = giv.getId();

    when(groupInVisitRepository.findById(givId)).thenReturn(Optional.of(giv));
    when(visitRepository.findByGroupInVisitId(givId)).thenReturn(List.of());

    assertThatThrownBy(() -> visitGroupService.getById(givId))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode().value()).isEqualTo(404);
            });
  }

  // ── getAll ────────────────────────────────────────────────────────────────────

  @Test
  void getAll_returnsPaginatedList() {
    Group grp = group("Queen");
    GroupInVisit giv = groupInVisit(grp);
    AccessPoint ap = building();
    Visit v1 = visit(personInRole(person("Brian", "May")), giv, ap, VisitStatus.PLANNED);
    Pageable pageable = PageRequest.of(0, 20);

    when(groupInVisitRepository.findAllFiltered(null, null, null, pageable))
        .thenReturn(new PageImpl<>(List.of(giv), pageable, 1));
    when(visitRepository.findByGroupInVisitId(giv.getId())).thenReturn(List.of(v1));

    var result = visitGroupService.getAll(null, null, pageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().groupName()).isEqualTo("Queen");
    assertThat(result.getContent().getFirst().memberCount()).isEqualTo(1);
  }

  @Test
  void getAll_emptyResult_returnsEmptyPage() {
    Pageable pageable = PageRequest.of(0, 20);
    when(groupInVisitRepository.findAllFiltered(null, null, null, pageable))
        .thenReturn(new PageImpl<>(List.of(), pageable, 0));

    var result = visitGroupService.getAll(null, null, pageable);

    assertThat(result.getTotalElements()).isZero();
    assertThat(result.getContent()).isEmpty();
  }

  // ── cancel ────────────────────────────────────────────────────────────────────

  @Test
  void cancel_cancelsOnlyPreRegisteredVisits() {
    Group grp = group("Queen");
    GroupInVisit giv = groupInVisit(grp);
    AccessPoint ap = building();
    Visit preRegistered = visit(personInRole(person("Brian", "May")), giv, ap, VisitStatus.PLANNED);
    Visit active = visit(personInRole(person("Roger", "Taylor")), giv, ap, VisitStatus.IN_BUILDING);
    Visit completed =
        visit(personInRole(person("Freddie", "Mercury")), giv, ap, VisitStatus.DEPARTED);

    when(groupInVisitRepository.findById(giv.getId())).thenReturn(Optional.of(giv));
    when(visitRepository.findByGroupInVisitId(giv.getId()))
        .thenReturn(List.of(preRegistered, active, completed));

    visitGroupService.cancel(giv.getId());

    assertThat(preRegistered.getStatus()).isEqualTo(VisitStatus.EXPIRED);
    assertThat(active.getStatus()).isEqualTo(VisitStatus.IN_BUILDING);
    assertThat(completed.getStatus()).isEqualTo(VisitStatus.DEPARTED);
    verify(visitRepository, times(1)).save(any());
  }

  @Test
  void cancel_allPreRegistered_cancelsAll() {
    Group grp = group("Queen");
    GroupInVisit giv = groupInVisit(grp);
    AccessPoint ap = building();
    Visit v1 = visit(personInRole(person("Brian", "May")), giv, ap, VisitStatus.PLANNED);
    Visit v2 = visit(personInRole(person("Roger", "Taylor")), giv, ap, VisitStatus.PLANNED);

    when(groupInVisitRepository.findById(giv.getId())).thenReturn(Optional.of(giv));
    when(visitRepository.findByGroupInVisitId(giv.getId())).thenReturn(List.of(v1, v2));

    visitGroupService.cancel(giv.getId());

    assertThat(v1.getStatus()).isEqualTo(VisitStatus.EXPIRED);
    assertThat(v2.getStatus()).isEqualTo(VisitStatus.EXPIRED);
    verify(visitRepository, times(2)).save(any());
  }

  @Test
  void cancel_notFound_throws404() {
    UUID fakeId = UUID.randomUUID();
    when(groupInVisitRepository.findById(fakeId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> visitGroupService.cancel(fakeId))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode().value()).isEqualTo(404);
            });
  }

  @Test
  void cancel_nonePreRegistered_savesNothing() {
    Group grp = group("Queen");
    GroupInVisit giv = groupInVisit(grp);
    AccessPoint ap = building();
    Visit active = visit(personInRole(person("Roger", "Taylor")), giv, ap, VisitStatus.IN_BUILDING);
    Visit completed =
        visit(personInRole(person("Freddie", "Mercury")), giv, ap, VisitStatus.DEPARTED);

    when(groupInVisitRepository.findById(giv.getId())).thenReturn(Optional.of(giv));
    when(visitRepository.findByGroupInVisitId(giv.getId())).thenReturn(List.of(active, completed));

    visitGroupService.cancel(giv.getId());

    verify(visitRepository, never()).save(any());
    assertThat(active.getStatus()).isEqualTo(VisitStatus.IN_BUILDING);
    assertThat(completed.getStatus()).isEqualTo(VisitStatus.DEPARTED);
  }
}
