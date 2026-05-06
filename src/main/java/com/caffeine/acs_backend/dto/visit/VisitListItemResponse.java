package com.caffeine.acs_backend.dto.visit;

import com.caffeine.acs_backend.entity.VisitStatusConverter;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.repository.VisitListView;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Schema(description = "Visit summary for paginated list view")
public record VisitListItemResponse(
    @Schema(description = "Visit ID") UUID id,
    @Schema(description = "Full name of the visitor", example = "John Smith") String fullName,
    @Schema(description = "Visitor's document number, null if none on record")
        String documentNumber,
    @Schema(
            description = "Organization of the visitor",
            example = "Acme Corp",
            accessMode = Schema.AccessMode.READ_ONLY)
        String organizationName,
    @Schema(description = "Full name of the host, null if no host assigned") String hostName,
    @Schema(description = "Recorded entry time") LocalDateTime entryTime,
    @Schema(description = "Recorded exit time, null if still inside") LocalDateTime exitTime,
    @Schema(description = "Status: in_building, departed, or expired", example = "in_building")
        VisitStatus status,
    @Schema(description = "person.id of the visitor") UUID visitorId,
    @Schema(description = "ID of the access point") UUID accessPointId,
    @Schema(description = "Name of the access point") String accessPointName,
    @Schema(description = "Address of the access point") String accessPointAddress) {

  private static final Logger log = LoggerFactory.getLogger(VisitListItemResponse.class);

  public static VisitListItemResponse from(VisitListView view) {

    String dbVisitStatus = view.getVisitStatus();
    VisitStatus visitStatusEnum = null;

    if (dbVisitStatus != null) {
      try {
        visitStatusEnum = VisitStatusConverter.parseDatabaseValue(dbVisitStatus);
      } catch (IllegalStateException e) {
        log.error("Unknown visit status '{}' found for visit {}", dbVisitStatus, view.getId(), e);
      }
    }

    return new VisitListItemResponse(
        view.getId(),
        view.getFullName(),
        view.getDocumentNumber(),
        view.getOrganizationName(),
        view.getHostName(),
        view.getEntryTime(),
        view.getExitTime(),
        visitStatusEnum,
        view.getVisitorId(),
        view.getAccessPointId(),
        view.getAccessPointName(),
        view.getAccessPointAddress());
  }
}
