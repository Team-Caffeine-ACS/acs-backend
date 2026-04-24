package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.visit.CreateVisitRequest;
import com.caffeine.acs_backend.dto.visit.CreateVisitResponse;
import com.caffeine.acs_backend.dto.visit.EditVisitRequest;
import com.caffeine.acs_backend.dto.visit.ExitVisitRequest;
import com.caffeine.acs_backend.dto.visit.VisitDetailResponse;
import com.caffeine.acs_backend.dto.visit.VisitListItemResponse;
import com.caffeine.acs_backend.dto.visit.VisitTimelineEntry;
import com.caffeine.acs_backend.entity.*;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisitService {

  private static final String VISITOR_ROLE_NAME = "Visitor";

  private final AccessPointRepository accessPointRepository;
  private final PersonInRoleRepository personInRoleRepository;
  private final KeycardRepository keycardRepository;
  private final KeycardInPossessionRepository keycardInPossessionRepository;
  private final RoleRepository roleRepository;
  private final PersonRepository personRepository;
  private final VisitRepository visitRepository;

  // ── GET /visits ──────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public Page<VisitListItemResponse> getVisits(
      String search,
      String status,
      LocalDateTime dateFrom,
      LocalDateTime dateTo,
      Pageable pageable) {
    String searchParam = (search == null || search.isBlank()) ? null : search.trim();
    String statusParam = (status == null || status.isBlank()) ? null : status.trim().toLowerCase();
    return visitRepository
        .findAllFiltered(searchParam, statusParam, dateFrom, dateTo, pageable)
        .map(VisitListItemResponse::from);
  }

  // ── GET /visits/{visitId} ────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public VisitDetailResponse getVisit(UUID visitId) {
    Visit visit = findVisitOrThrow(visitId);

    Person person = visit.getVisitor().getPerson();

    String hostName = null;
    if (visit.getHost() != null) {
      Person host = visit.getHost().getPerson();
      hostName = host.getGivenName() + " " + host.getSurname();
    }

    UUID cardId =
        keycardInPossessionRepository
            .findActiveByHolder(visit.getVisitor())
            .map(kip -> kip.getKeycard().getId())
            .orElse(null);

    return new VisitDetailResponse(
        visit.getId(),
        person.getGivenName(),
        person.getSurname(),
        person.getSocialSecurityNumber(),
        person.getOrganization() != null ? person.getOrganization().getName() : null,
        person.getDepartment() != null ? person.getDepartment().getName() : null,
        hostName,
        visit.getComment(),
        cardId);
  }

  // ── PUT /visits/{visitId}/exit ────────────────────────────────────────────────

  @Transactional
  public VisitDetailResponse exitVisit(UUID visitId, ExitVisitRequest request) {
    Visit visit = findVisitOrThrow(visitId);

    if (visit.getExitTime() != null) {
      throw new BusinessException(
          "Visit already has an exit time recorded",
          ErrorCode.BUSINESS_RULE_VIOLATION,
          HttpStatus.CONFLICT);
    }

    visit.setExitTime(request.exitTime());
    visitRepository.save(visit);

    return buildDetailResponse(visit);
  }

  // ── PUT /visits/{visitId}/edit ────────────────────────────────────────────────

    @Transactional
    public VisitDetailResponse editVisit(UUID visitId, EditVisitRequest request) {
    Visit visit = findVisitOrThrow(visitId);

    if (request.entryTime() != null) {
        validateEditedEntryTime(visit, request.entryTime());
        visit.setArrivalTime(request.entryTime());
    }

    if (request.exitTime() != null) {
    visit.setExitTime(request.exitTime());
    }

    PersonInRole assignor = findActivePersonInRoleOrThrow(request.assignorId(), "Assignor");
    visit.setAssignor(assignor);

    if (request.accessPointId() != null) {
        AccessPoint accessPoint = findAccessPointOrThrow(request.accessPointId());
        visit.setAccessPoint(accessPoint);
    }

    if (request.hostId() != null) {
        visit.setHost(resolveHostOrNull(request.hostId()));
    }

    if (request.comment() != null) {
        visit.setComment(request.comment());
    }

    visitRepository.save(visit);
    return buildDetailResponse(visit);
    }

  // ── GET /visits/{visitId}/timeline ────────────────────────────────────────────

  @Transactional(readOnly = true)
  public List<VisitTimelineEntry> getTimeline(UUID visitId) {
    Visit visit = findVisitOrThrow(visitId);

    List<VisitTimelineEntry> entries = new ArrayList<>();

    entries.add(
        new VisitTimelineEntry(visit.getArrivalTime(), "ARRIVAL_REGISTERED", visit.getComment()));

    if (visit.getExitTime() != null) {
      entries.add(new VisitTimelineEntry(visit.getExitTime(), "DEPARTURE_REGISTERED", null));
    }

    return entries;
  }

  // ── Legacy createVisit (kept for backwards compatibility) ────────────────────

  @Transactional
  public CreateVisitResponse createVisit(CreateVisitRequest request, User assignorUser) {

    Person person =
        personRepository
            .findById(request.personId())
            .orElseThrow(
                () -> new IllegalArgumentException("Person not found: " + request.personId()));

    AccessPoint accessPoint =
        accessPointRepository
            .findById(request.accessPointId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Access point not found: " + request.accessPointId()));

    Keycard keycard = null;
    if (request.keycardId() != null) {
      keycard =
          keycardRepository
              .findById(request.keycardId())
              .orElseThrow(
                  () -> new IllegalArgumentException("Keycard not found: " + request.keycardId()));

      if (!keycard.isActive()) {
        throw new IllegalArgumentException("Keycard is not active: " + request.keycardId());
      }
      if (keycardInPossessionRepository.existsByKeycardAndReturnTimeIsNull(keycard)) {
        throw new IllegalArgumentException(
            "Keycard is already assigned to someone: " + keycard.getKeycardNumber());
      }
    }

    PersonInRole assignor = resolveAssignorFromUser(assignorUser);

    PersonInRole host = null;
    if (request.hostPersonInRoleId() != null) {
      host =
          personInRoleRepository
              .findById(request.hostPersonInRoleId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Host not found: " + request.hostPersonInRoleId()));
    }

    Role visitorRole = findVisitorRole();
    PersonInRole visitorPersonInRole =
        personInRoleRepository
            .findByPersonAndRoleAndIsActiveTrue(person, visitorRole)
            .orElseGet(
                () ->
                    personInRoleRepository.save(
                        PersonInRole.builder().person(person).role(visitorRole).build()));

    LocalDateTime now = request.arrivalTime() != null ? request.arrivalTime() : LocalDateTime.now();

    Visit visit =
        Visit.builder()
            .arrivalTime(now)
            .accessPoint(accessPoint)
            .visitor(visitorPersonInRole)
            .assignor(assignor)
            .host(host)
            .comment(request.comment())
            .build();
    visitRepository.save(visit);

    KeycardInPossession possession = null;
    if (keycard != null) {
      possession =
          KeycardInPossession.builder()
              .keycard(keycard)
              .assignedTime(now)
              .keycardHolder(visitorPersonInRole)
              .keycardAssignor(assignor)
              .assigningAccessPoint(accessPoint)
              .build();
      keycardInPossessionRepository.save(possession);
    }

    String hostName = null;
    if (host != null) {
      Person hostPerson = host.getPerson();
      hostName = hostPerson.getGivenName() + " " + hostPerson.getSurname();
    }

    UUID cardId =
        keycardInPossessionRepository
            .findActiveByHolder(visitorPersonInRole)
            .map(kip -> kip.getKeycard().getId())
            .orElse(null);

    return new CreateVisitResponse(
        visit.getId(),
        person.getId(),
        visitorPersonInRole.getId(),
        person.getGivenName(),
        person.getSurname(),
        person.getSocialSecurityNumber(),
        person.getOrganization() != null ? person.getOrganization().getName() : null,
        person.getDepartment() != null ? person.getDepartment().getName() : null,
        hostName,
        visit.getComment(),
        now,
        cardId,
        possession != null ? possession.getId() : null,
        keycard != null ? keycard.getKeycardNumber() : null);
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private Visit findVisitOrThrow(UUID visitId) {
    return visitRepository
        .findById(visitId)
        .orElseThrow(
            () ->
                new BusinessException(
                    "Visit not found: " + visitId,
                    ErrorCode.RESOURCE_NOT_FOUND,
                    HttpStatus.NOT_FOUND));
  }

  private PersonInRole findPersonInRoleOrThrow(UUID personInRoleId) {
    return personInRoleRepository
        .findById(personInRoleId)
        .orElseThrow(
            () ->
                new BusinessException(
                    "PersonInRole not found: " + personInRoleId,
                    ErrorCode.RESOURCE_NOT_FOUND,
                    HttpStatus.NOT_FOUND));
  }

  private PersonInRole findActivePersonInRoleOrThrow(UUID personInRoleId, String roleLabel) {
    PersonInRole personInRole = findPersonInRoleOrThrow(personInRoleId);
    if (!personInRole.isActive()) {
      throw new BusinessException(
          roleLabel + " must reference an active role assignment: " + personInRoleId,
          ErrorCode.RESOURCE_NOT_FOUND,
          HttpStatus.NOT_FOUND);
    }
    return personInRole;
  }

  private AccessPoint findAccessPointOrThrow(UUID accessPointId) {
    return accessPointRepository
        .findById(accessPointId)
        .orElseThrow(
            () ->
                new BusinessException(
                    "Access point not found: " + accessPointId,
                    ErrorCode.RESOURCE_NOT_FOUND,
                    HttpStatus.NOT_FOUND));
  }

  private PersonInRole resolveHostOrNull(UUID hostId) {
    if (hostId == null) {
      return null;
    }

    Person hostPerson =
        personRepository
            .findById(hostId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        "Host person not found: " + hostId,
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND));

    return personInRoleRepository
        .findFirstByPersonAndIsActiveTrue(hostPerson)
        .orElseThrow(
            () ->
                new BusinessException(
                    "Host has no active role assignment: " + hostId,
                    ErrorCode.RESOURCE_NOT_FOUND,
                    HttpStatus.NOT_FOUND));
  }

  private void validateEditedEntryTime(Visit visit, LocalDateTime newEntryTime) {
    if (visit.getExitTime() != null && newEntryTime.isAfter(visit.getExitTime())) {
      throw new BusinessException(
          "Entry time cannot be later than the recorded exit time",
          ErrorCode.BUSINESS_RULE_VIOLATION,
          HttpStatus.CONFLICT);
    }
  }

  private Role findVisitorRole() {
    return roleRepository
        .findByName(VISITOR_ROLE_NAME)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "VISITOR role not configured in database. Please seed the roles table."));
  }

  private VisitDetailResponse buildDetailResponse(Visit visit) {
    Person person = visit.getVisitor().getPerson();

    String hostName = null;
    if (visit.getHost() != null) {
      Person host = visit.getHost().getPerson();
      hostName = host.getGivenName() + " " + host.getSurname();
    }

    UUID cardId =
        keycardInPossessionRepository
            .findActiveByHolder(visit.getVisitor())
            .map(kip -> kip.getKeycard().getId())
            .orElse(null);

    return new VisitDetailResponse(
        visit.getId(),
        person.getGivenName(),
        person.getSurname(),
        person.getSocialSecurityNumber(),
        person.getOrganization() != null ? person.getOrganization().getName() : null,
        person.getDepartment() != null ? person.getDepartment().getName() : null,
        hostName,
        visit.getComment(),
        cardId);
  }

  private PersonInRole resolveAssignorFromUser(User assignorUser) {
    if (assignorUser.getPerson() == null) {
      throw new IllegalStateException(
          "Your user account has no linked Person. Ask an administrator to link your profile.");
    }
    return personInRoleRepository
        .findFirstByPersonAndIsActiveTrue(assignorUser.getPerson())
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "No active role assignment found for your account."
                        + " Ask an administrator to assign you a role."));
  }
}
