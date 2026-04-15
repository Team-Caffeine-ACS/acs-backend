package com.caffeine.acs_backend.dto.visitor;

import com.caffeine.acs_backend.dto.preregistration.PreRegistrationResponse;
import com.caffeine.acs_backend.enums.VisitStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduledVisitorResponse(
    UUID visitorId,
    LocalDateTime scheduledTime,
    String fullName,
    String documentType,
    String hostName,
    VisitStatus status) {

  public static ScheduledVisitorResponse from(PreRegistrationResponse r) {
    return new ScheduledVisitorResponse(
        r.id(), r.expectedArrival(), r.fullName(), null, r.hostName(), r.status());
  }
}
