package com.caffeine.acs_backend.dto.keycard;

import com.caffeine.acs_backend.entity.Keycard;
import com.caffeine.acs_backend.entity.KeycardInPossession;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Full keycard details")
public record KeycardDetailResponse(
    @Schema(description = "Unique identifier") UUID id,
    @Schema(description = "Keycard number printed on the card", example = "KC-0001")
        String keycardNumber,
    @Schema(description = "Whether the keycard is active") boolean active,
    @Schema(description = "Expiration date/time, null means no expiry") LocalDateTime validUntil,
    @Schema(description = "Full name of the person currently holding the card, null if available")
        String assignedUser,
    @Schema(description = "UUID of the active person_in_role holding the card")
        UUID assignedPersonInRoleId,
    @Schema(description = "When the card was assigned to the current holder")
        LocalDateTime assignedTime,
    @Schema(description = "When the card was last returned, null if never returned")
        LocalDateTime lastReturnTime) {

  public static KeycardDetailResponse from(
      Keycard keycard, KeycardInPossession activePossession, LocalDateTime lastReturnTime) {
    String assignedUser = null;
    UUID assignedPersonInRoleId = null;
    LocalDateTime assignedTime = null;
    if (activePossession != null) {
      var person = activePossession.getKeycardHolder().getPerson();
      assignedUser = person.getGivenName() + " " + person.getSurname();
      assignedPersonInRoleId = activePossession.getKeycardHolder().getId();
      assignedTime = activePossession.getAssignedTime();
    }
    return new KeycardDetailResponse(
        keycard.getId(),
        keycard.getKeycardNumber(),
        keycard.isActive(),
        keycard.getValidUntil(),
        assignedUser,
        assignedPersonInRoleId,
        assignedTime,
        lastReturnTime);
  }
}
