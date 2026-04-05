package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.visit.CreateVisitRequest;
import com.caffeine.acs_backend.dto.visit.CreateVisitResponse;
import com.caffeine.acs_backend.entity.*;
import com.caffeine.acs_backend.repository.*;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisitService {

  private static final String VISITOR_ROLE_NAME = "VISITOR";

  private final AccessPointRepository accessPointRepository;
  private final PersonInRoleRepository personInRoleRepository;
  private final KeycardRepository keycardRepository;
  private final KeycardInPossessionRepository keycardInPossessionRepository;
  private final RoleRepository roleRepository;
  private final PersonRepository personRepository;
  private final VisitRepository visitRepository;

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

    PersonInRole assignor = resolveAssignor(assignorUser);

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

    Role visitorRole =
        roleRepository
            .findByName(VISITOR_ROLE_NAME)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "VISITOR role not configured in database. Please seed the roles table."));

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

    return new CreateVisitResponse(
        visit.getId(),
        person.getId(),
        visitorPersonInRole.getId(),
        possession != null ? possession.getId() : null,
        keycard != null ? keycard.getKeycardNumber() : null,
        now);
  }

  private PersonInRole resolveAssignor(User assignorUser) {
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
