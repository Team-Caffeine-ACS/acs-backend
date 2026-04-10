package com.caffeine.acs_backend.dto.keycard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Request body for returning a keycard")
public record ReturnKeycardRequest(
    @Schema(description = "Access point ID where the card is being returned") @NotNull
        UUID returnAccessPointId) {}
