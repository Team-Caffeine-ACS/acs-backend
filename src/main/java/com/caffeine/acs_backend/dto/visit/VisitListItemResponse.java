package com.caffeine.acs_backend.dto.visit;

import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.repository.VisitListView;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Visit summary for paginated list view")
public record VisitListItemResponse(
    @Schema(description = "Visit ID") UUID id,
    @Schema(description = "Full name of the visitor", example = "John Smith") String fullName,
    @Schema(description = "Visitor's document number, null if none on record")
        String documentNumber,
    @Schema(description = "Organization of the visitor", example = "Acme Corp") String organization,
    @Schema(description = "Full name of the host, null if no host assigned") String hostName,
    @Schema(description = "Recorded entry time") LocalDateTime entryTime,
    @Schema(description = "Recorded exit time, null if still inside") LocalDateTime exitTime,
    @Schema(description = "Status: in_building, departed, or expired", example = "in_building")
        VisitStatus status,
    @Schema(description = "person.id of the visitor") UUID visitorId,
    @Schema(description = "ID of the access point") UUID accessPointId) {

  public static VisitListItemResponse from(VisitListView view) {
    return new VisitListItemResponse(
        view.getId(),
        view.getFullName(),
        view.getDocumentNumber(),
        view.getOrganization(),
        view.getHostName(),
        view.getEntryTime(),
        view.getExitTime(),
        view.getStatus() != null ? VisitStatus.valueOf(view.getStatus()) : null,
        view.getVisitorId(),
        view.getAccessPointId());
  }
}
