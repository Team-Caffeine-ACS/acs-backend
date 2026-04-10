package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.preregistration.CreatePreRegistrationRequest;
import com.caffeine.acs_backend.dto.preregistration.CreatePreRegistrationResponse;
import com.caffeine.acs_backend.dto.preregistration.NotifyRequest;
import com.caffeine.acs_backend.dto.preregistration.PreRegistrationResponse;
import com.caffeine.acs_backend.dto.preregistration.UpdatePreRegistrationRequest;
import com.caffeine.acs_backend.entity.AccessPoint;
import com.caffeine.acs_backend.entity.Person;
import com.caffeine.acs_backend.entity.PersonInRole;
import com.caffeine.acs_backend.entity.Role;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.entity.Visit;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.repository.AccessPointRepository;
import com.caffeine.acs_backend.repository.PersonInRoleRepository;
import com.caffeine.acs_backend.repository.PersonRepository;
import com.caffeine.acs_backend.repository.RoleRepository;
import com.caffeine.acs_backend.repository.UserRepository;
import com.caffeine.acs_backend.repository.VisitRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreRegistrationService {

  private final VisitRepository visitRepository;
  private final AccessPointRepository accessPointRepository;
  private final UserRepository userRepository;
  private final EmailService emailService;
  private final PersonInRoleRepository personInRoleRepository;
  private final RoleRepository roleRepository;
  private final PersonRepository personRepository;

  @Transactional
  public CreatePreRegistrationResponse create(CreatePreRegistrationRequest request) {
    AccessPoint building =
        accessPointRepository
            .findById(request.buildingId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Building not found"));

    User currentUser = getCurrentUser();

    Person person =
        personRepository
            .findById(request.personId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Person not found"));

    Role visitorRole =
        roleRepository
            .findByName("Visitor")
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Visitor role not found"));

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
        Visit.builder()
            .arrivalTime(request.expectedArrival())
            .accessPoint(building)
            .visitor(personInRole)
            .notes(request.notes())
            .status(VisitStatus.PRE_REGISTERED)
            .build();

    if (request.hostId() != null) {
      personInRoleRepository.findById(request.hostId()).ifPresent(visit::setHost);
    }

    visitRepository.save(visit);

    if (person.getEmail() != null && !person.getEmail().isBlank()) {
      emailService.sendVisitorNotification(
          person.getEmail(),
          person.getGivenName() + " " + person.getSurname(),
          request.expectedArrival().toString(),
          building.getName(),
          null);
    }

    log.info("Pre-registration created: {} by user {}", visit.getId(), currentUser.getEmail());

    return new CreatePreRegistrationResponse(visit.getId(), visit.getStatus());
  }

  public PreRegistrationResponse getById(UUID id) {
    Visit visit = findPreRegistration(id);
    return PreRegistrationResponse.from(visit, getCurrentUser());
  }

  public Page<PreRegistrationResponse> getAll(
      LocalDate date, String search, VisitStatus status, UUID buildingId, Pageable pageable) {

    LocalDateTime from = date != null ? date.atStartOfDay() : null;
    LocalDateTime to = date != null ? date.plusDays(1).atStartOfDay() : null;

    return visitRepository
        .findPreRegistrations(
            status != null ? status.name() : VisitStatus.PRE_REGISTERED.name(),
            buildingId,
            from,
            to,
            search,
            pageable)
        .map(visit -> PreRegistrationResponse.from(visit, getCurrentUser()));
  }

  @Transactional
  public PreRegistrationResponse update(UUID id, UpdatePreRegistrationRequest request) {
    Visit visit = findPreRegistration(id);

    if (request.expectedArrival() != null) {
      visit.setArrivalTime(request.expectedArrival());
    }
    if (request.notes() != null) {
      visit.setNotes(request.notes());
    }
    if (request.buildingId() != null) {
      AccessPoint building =
          accessPointRepository
              .findById(request.buildingId())
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Building not found"));
      visit.setAccessPoint(building);
    }

    visitRepository.save(visit);
    return PreRegistrationResponse.from(visit, getCurrentUser());
  }

  @Transactional
  public void cancel(UUID id, String customMessage) {
    Visit visit = findPreRegistration(id);
    visit.setStatus(VisitStatus.CANCELLED);
    visitRepository.save(visit);

    log.info("Pre-registration cancelled: {}", id);

    String email = visit.getVisitor() != null ? visit.getVisitor().getPerson().getEmail() : null;

    if (email != null) {
      emailService.sendCancellationNotification(email, "Visitor", customMessage);
    }
  }

  public void resendNotification(UUID id, NotifyRequest request) {
    Visit visit = findPreRegistration(id);

    String email = visit.getVisitor() != null ? visit.getVisitor().getPerson().getEmail() : null;

    if (email == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "No email on file for this visitor");
    }

    Person person = visit.getVisitor().getPerson();

    emailService.sendVisitorNotification(
        person.getEmail(),
        person.getGivenName() + " " + person.getSurname(),
        visit.getArrivalTime().toString(),
        visit.getAccessPoint().getName(),
        request != null ? request.message() : null);
  }

  private Visit findPreRegistration(UUID id) {
    Visit visit =
        visitRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pre-registration not found"));
    if (visit.getStatus() == VisitStatus.CANCELLED) {
      throw new ResponseStatusException(HttpStatus.GONE, "Pre-registration is cancelled");
    }
    return visit;
  }

  private User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }
}
