package com.caffeine.acs_backend.dto.keycard;

import com.caffeine.acs_backend.entity.Keycard;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Available keycard for assignment")
public record KeycardResponse(
    @Schema(description = "Unique identifier") UUID id,
    @Schema(description = "Keycard number printed on the card", example = "CARD-0042")
        String keycardNumber,
    @Schema(description = "Expiration date/time of the keycard, null means no expiry")
        LocalDateTime validUntil) {

  public static KeycardResponse from(Keycard keycard) {
    return new KeycardResponse(
        keycard.getId(), keycard.getKeycardNumber(), keycard.getValidUntil());
  }
}
