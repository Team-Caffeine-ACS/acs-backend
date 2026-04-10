package com.caffeine.acs_backend.dto.keycard;

import com.caffeine.acs_backend.repository.KeycardListView;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Keycard summary for paginated list view")
public record KeycardListItemResponse(
    @Schema(description = "Unique identifier") UUID id,
    @Schema(description = "Keycard number printed on the card", example = "KC-0001")
        String keycardNumber,
    @Schema(description = "Status: AVAILABLE, IN_USE, DISABLED, or EXPIRED", example = "AVAILABLE")
        String status,
    @Schema(
            description = "Full name of the person currently holding the card, null if not in use",
            example = "John Smith")
        String assignedUser,
    @Schema(description = "When the card was last returned, null if never returned")
        LocalDateTime lastReturnTime) {

  public static KeycardListItemResponse from(KeycardListView view) {
    return new KeycardListItemResponse(
        view.getId(),
        view.getKeycardNumber(),
        view.getStatus(),
        view.getAssignedUser(),
        view.getLastReturnTime());
  }
}
