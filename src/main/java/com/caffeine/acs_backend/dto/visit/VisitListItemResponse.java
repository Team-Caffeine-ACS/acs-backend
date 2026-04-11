package com.caffeine.acs_backend.dto.visit;

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
    @Schema(description = "Full name of the host, null if no host assigned") String hostName,
    @Schema(description = "Recorded entry time") LocalDateTime entryTime,
    @Schema(description = "Recorded exit time, null if still inside") LocalDateTime exitTime,
    @Schema(description = "Status: in_building, departed, or expired", example = "in_building")
        String status,
    @Schema(description = "person.id of the visitor") UUID visitorId) {

  public static VisitListItemResponse from(VisitListView view) {
    return new VisitListItemResponse(
        view.getId(),
        view.getFullName(),
        view.getDocumentNumber(),
        view.getHostName(),
        view.getEntryTime(),
        view.getExitTime(),
        view.getStatus(),
        view.getVisitorId());
  }
}
