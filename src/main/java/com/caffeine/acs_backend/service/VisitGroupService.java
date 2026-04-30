package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.visitgroup.CreateGroupVisitRequest;
import com.caffeine.acs_backend.dto.visitgroup.GroupMemberResponse;
import com.caffeine.acs_backend.dto.visitgroup.GroupVisitListItemResponse;
import com.caffeine.acs_backend.dto.visitgroup.GroupVisitResponse;
import com.caffeine.acs_backend.entity.*;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitGroupService {

  private final GroupRepository groupRepository;
  private final GroupInVisitRepository groupInVisitRepository;
  private final VisitRepository visitRepository;
  private final AccessPointRepository accessPointRepository;
  private final PersonRepository personRepository;
  private final PersonInRoleRepository personInRoleRepository;
  private final RoleRepository roleRepository;
  private final EmailService emailService;

  // ── POST — create group visit ────────────────────────────────────────────────

  @Transactional
  public GroupVisitResponse create(CreateGroupVisitRequest request) {
    AccessPoint building =
        accessPointRepository
            .findById(request.buildingId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Building not found"));

    Role visitorRole =
        roleRepository
            .findByName("Visitor")
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Visitor role not found"));

    // Create or reuse group by name
    Group group =
        groupRepository
            .findByName(request.groupName())
            .orElseGet(
                () ->
                    groupRepository.save(
                        Group.builder()
                            .name(request.groupName())
                            .description(request.groupDescription())
                            .build()));

    // Create the group-in-visit (the shared visit event)
    GroupInVisit groupInVisit =
        groupInVisitRepository.save(
            GroupInVisit.builder()
                .group(group)
                .plannedArrival(request.expectedArrival())
                .plannedExit(request.expectedExit())
                .comment(request.comment())
                .build());

    // Resolve host if provided
    PersonInRole host = resolveHostOrNull(request.hostId());

    // Create individual Visit per person
    List<Visit> visits =
        request.personIds().stream()
            .map(
                personId -> {
                  Person person =
                      personRepository
                          .findById(personId)
                          .orElseThrow(
                              () ->
                                  new ResponseStatusException(
                                      HttpStatus.BAD_REQUEST, "Person not found: " + personId));

                  PersonInRole personInRole =
                      personInRoleRepository
                          .findByPersonAndRoleAndIsActiveTrue(person, visitorRole)
                          .orElseGet(
                              () ->
                                  personInRoleRepository.save(
                                      PersonInRole.builder()
                                          .person(person)
                                          .role(visitorRole)
                                          .isActive(true)
                                          .build()));

                  Visit visit =
                      visitRepository.save(
                          Visit.builder()
                              .arrivalTime(request.expectedArrival())
                              .accessPoint(building)
                              .visitor(personInRole)
                              .groupInVisit(groupInVisit)
                              .host(host)
                              .notes(request.comment())
                              .status(VisitStatus.PRE_REGISTERED)
                              .build());

                  sendNotificationIfEmailPresent(person, request.expectedArrival(), building);

                  return visit;
                })
            .toList();

    log.info(
        "Group visit created: groupInVisitId={}, groupName={}, members={}",
        groupInVisit.getId(),
        group.getName(),
        visits.size());

    return buildGroupVisitResponse(groupInVisit, visits, building, host);
  }

  // ── GET — single group visit with members ────────────────────────────────────

  @Transactional(readOnly = true)
  public GroupVisitResponse getById(UUID groupInVisitId) {
    GroupInVisit groupInVisit =
        groupInVisitRepository
            .findById(groupInVisitId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group visit not found"));

    List<Visit> visits = visitRepository.findByGroupInVisitId(groupInVisitId);

    if (visits.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "No visits found for this group visit");
    }

    // Derive building and host from the first visit
    AccessPoint building = visits.getFirst().getAccessPoint();
    PersonInRole host = visits.getFirst().getHost();

    return buildGroupVisitResponse(groupInVisit, visits, building, host);
  }

  // ── GET — paginated list of group visits ─────────────────────────────────────

  @Transactional(readOnly = true)
  public Page<GroupVisitListItemResponse> getAll(LocalDate date, String search, Pageable pageable) {

    String searchParam = (search == null || search.isBlank()) ? null : search.trim();
    LocalDateTime from = date != null ? date.atStartOfDay() : null;
    LocalDateTime to = date != null ? date.plusDays(1).atStartOfDay() : null;

    return groupInVisitRepository
        .findAllFiltered(searchParam, from, to, pageable)
        .map(this::buildListItemResponse);
  }

  // ── DELETE — cancel all visits in the group ──────────────────────────────────

  @Transactional
  public void cancel(UUID groupInVisitId) {
    GroupInVisit groupInVisit =
        groupInVisitRepository
            .findById(groupInVisitId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group visit not found"));

    List<Visit> visits = visitRepository.findByGroupInVisitId(groupInVisitId);

    visits.forEach(
        visit -> {
          if (visit.getStatus() == VisitStatus.PRE_REGISTERED) {
            visit.setStatus(VisitStatus.CANCELLED);
            visitRepository.save(visit);
          }
        });

    log.info("Group visit cancelled: groupInVisitId={}", groupInVisitId);
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private PersonInRole resolveHostOrNull(UUID hostId) {
    if (hostId == null) {
      return null;
    }

    Person hostPerson =
        personRepository
            .findById(hostId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Host person not found: " + hostId));

    return personInRoleRepository
        .findFirstByPersonAndIsActiveTrue(hostPerson)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Host has no active role assignment: " + hostId));
  }

  private void sendNotificationIfEmailPresent(
      Person person, LocalDateTime arrival, AccessPoint building) {
    if (person.getEmail() != null && !person.getEmail().isBlank()) {
      emailService.sendVisitorNotification(
          person.getEmail(),
          person.getGivenName() + " " + person.getSurname(),
          arrival.toString(),
          building.getName(),
          null);
    }
  }

  private GroupVisitResponse buildGroupVisitResponse(
      GroupInVisit groupInVisit, List<Visit> visits, AccessPoint building, PersonInRole host) {

    List<GroupMemberResponse> members = visits.stream().map(this::buildMemberResponse).toList();

    String hostName = null;
    if (host != null) {
      Person hostPerson = host.getPerson();
      hostName = hostPerson.getGivenName() + " " + hostPerson.getSurname();
    }

    int checkedIn = (int) visits.stream().filter(v -> v.getStatus() == VisitStatus.ACTIVE).count();
    int departed =
        (int) visits.stream().filter(v -> v.getStatus() == VisitStatus.COMPLETED).count();

    return new GroupVisitResponse(
        groupInVisit.getId(),
        groupInVisit.getGroup().getName(),
        groupInVisit.getGroup().getDescription(),
        groupInVisit.getPlannedArrival(),
        groupInVisit.getPlannedExit(),
        groupInVisit.getComment(),
        building.getName(),
        hostName,
        members.size(),
        checkedIn,
        departed,
        members);
  }

  private GroupMemberResponse buildMemberResponse(Visit visit) {
    Person person = visit.getVisitor().getPerson();
    return new GroupMemberResponse(
        visit.getId(),
        person.getId(),
        person.getGivenName() + " " + person.getSurname(),
        person.getEmail(),
        person.getSocialSecurityNumber(),
        visit.getStatus(),
        visit.getArrivalTime(),
        visit.getExitTime());
  }

  private GroupVisitListItemResponse buildListItemResponse(GroupInVisit groupInVisit) {
    List<Visit> visits = visitRepository.findByGroupInVisitId(groupInVisit.getId());

    int checkedIn = (int) visits.stream().filter(v -> v.getStatus() == VisitStatus.ACTIVE).count();
    int departed =
        (int) visits.stream().filter(v -> v.getStatus() == VisitStatus.COMPLETED).count();

    return new GroupVisitListItemResponse(
        groupInVisit.getId(),
        groupInVisit.getGroup().getName(),
        groupInVisit.getPlannedArrival(),
        groupInVisit.getPlannedExit(),
        groupInVisit.getComment(),
        visits.size(),
        checkedIn,
        departed);
  }
}
