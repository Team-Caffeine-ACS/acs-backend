package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.preregistration.CreatePreRegistrationRequest;
import com.caffeine.acs_backend.dto.preregistration.CreatePreRegistrationResponse;
import com.caffeine.acs_backend.dto.preregistration.NotifyRequest;
import com.caffeine.acs_backend.dto.preregistration.PreRegistrationResponse;
import com.caffeine.acs_backend.dto.preregistration.UpdatePreRegistrationRequest;
import com.caffeine.acs_backend.entity.AccessPoint;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.entity.Visit;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.repository.AccessPointRepository;
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

  @Transactional
  public CreatePreRegistrationResponse create(CreatePreRegistrationRequest request) {
    AccessPoint building = accessPointRepository.findById(request.buildingId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Building not found"));

    User currentUser = getCurrentUser();

    Visit visit = Visit.builder()
        .arrivalTime(request.expectedArrival())
        .accessPoint(building)
        .visitorEmail(request.email())
        .visitorFullName(request.fullName())
        .notes(request.notes())
        .VisitAccessLevel(request.VisitAccessLevel())
        .status(VisitStatus.PRE_REGISTERED)
        .build();

    visitRepository.save(visit);

    if (request.email() != null && !request.email().isBlank()) {
      emailService.sendVisitorNotification(
          request.email(),
          request.fullName(),
          request.expectedArrival().toString(),
          building.getName());
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
          status != null ? status.name() : null,
          buildingId, from, to, search, pageable)
      .map(visit -> PreRegistrationResponse.from(visit, getCurrentUser()));  }

  @Transactional
  public PreRegistrationResponse update(UUID id, UpdatePreRegistrationRequest request) {
    Visit visit = findPreRegistration(id);

    if (request.expectedArrival() != null) {
      visit.setArrivalTime(request.expectedArrival());
    }
    if (request.notes() != null) {
      visit.setNotes(request.notes());
    }
    if (request.VisitAccessLevel() != null) {
      visit.setVisitAccessLevel(request.VisitAccessLevel());
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

    if (visit.getVisitorEmail() != null) {
      emailService.sendCancellationNotification(
          visit.getVisitorEmail(),
          "Visitor",
          customMessage);
    }
  }

  public void resendNotification(UUID id, NotifyRequest request) {
    Visit visit = findPreRegistration(id);

    if (visit.getVisitorEmail() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No email on file for this visitor");
    }

    emailService.sendVisitorNotification(
        visit.getVisitorEmail(),
        "Visitor",
        visit.getArrivalTime().toString(),
        visit.getAccessPoint().getName());
  }

  private Visit findPreRegistration(UUID id) {
    Visit visit = visitRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pre-registration not found"));
    if (visit.getStatus() == VisitStatus.CANCELLED) {
      throw new ResponseStatusException(HttpStatus.GONE, "Pre-registration is cancelled");
    }
    return visit;
  }

  private User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }
}