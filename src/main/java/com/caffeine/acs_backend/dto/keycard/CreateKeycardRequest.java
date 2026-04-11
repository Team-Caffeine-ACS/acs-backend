package com.caffeine.acs_backend.dto.keycard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "Request body for registering a new RFID keycard")
public record CreateKeycardRequest(
    @Schema(description = "Keycard number printed on the card", example = "KC-0042")
        @NotBlank
        @Size(max = 128)
        String keycardNumber,
    @Schema(description = "Expiration date/time, omit for no expiry") LocalDateTime validUntil,
    @Schema(description = "Initial active status", example = "true") boolean initialStatus) {}
