package com.caffeine.acs_backend.dto.preregistration;

import com.caffeine.acs_backend.enums.VisitAccessLevel;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.entity.Visit;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Full details of a pre-registration")
public record PreRegistrationResponse(
    UUID id,
    String fullName,
    String email,
    LocalDateTime expectedArrival,
    String hostName,
    String notes,
    VisitAccessLevel VisitAccessLevel,
    String building,
    LocalDateTime createdAt,
    VisitStatus status) {

  public static PreRegistrationResponse from(Visit visit, User currentUser) {
    String fullName = visit.getVisitor() != null
        ? visit.getVisitor().getPerson().getGivenName() + " " + visit.getVisitor().getPerson().getSurname()
        : visit.getVisitorFullName();

    String hostName = visit.getHost() != null
        ? visit.getHost().getPerson().getGivenName() + " " + visit.getHost().getPerson().getSurname()
        : currentUser != null ? currentUser.getEmail() : null;

    return new PreRegistrationResponse(
        visit.getId(),
        fullName,
        visit.getVisitorEmail(),
        visit.getArrivalTime(),
        hostName,
        visit.getNotes(),
        visit.getVisitAccessLevel(),
        visit.getAccessPoint().getName(),
        visit.getCreatedAt(),
        visit.getStatus());
}
}