package com.caffeine.acs_backend.dto.visit;

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
    @Schema(description = "ID of the access point") UUID accessPointId) {

  private static final Logger log = LoggerFactory.getLogger(VisitListItemResponse.class);

  public static VisitListItemResponse from(VisitListView view) {

    String dbVisitStatus = view.getVisitStatus();
    VisitStatus visitStatusEnum = null;

    if (dbVisitStatus != null) {
      try {
        // Teisendame: väike täht -> suur, tühikud välja
        visitStatusEnum = VisitStatus.valueOf(dbVisitStatus.trim().toUpperCase());
      } catch (IllegalArgumentException e) {
        // See on sinu "print" – näed konsoolis, mis andmebaasis tegelikult on
        log.error(
            "VIGA: Andmebaasis on tundmatu staatus: '{}' külastusel ID-ga: {}",
            dbVisitStatus,
            view.getId());
        // et testida, kas ülejäänud kood töötab.
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
        // view.getVisitStatus() != null ? VisitStatus.valueOf(view.getVisitStatus().toUpperCase())
        // : null,
        visitStatusEnum,
        view.getVisitorId(),
        view.getAccessPointId());
  }
}
