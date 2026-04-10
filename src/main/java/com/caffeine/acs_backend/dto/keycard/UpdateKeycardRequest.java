package com.caffeine.acs_backend.dto.keycard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "Request body for updating keycard metadata")
public record UpdateKeycardRequest(
    @Schema(description = "Updated keycard number", example = "KC-0042") @Size(max = 128)
        String keycardNumber,
    @Schema(description = "Updated active status — set false to deactivate (lost/stolen)")
        Boolean active,
    @Schema(description = "Updated expiration date/time, null clears the expiry")
        LocalDateTime validUntil) {}
