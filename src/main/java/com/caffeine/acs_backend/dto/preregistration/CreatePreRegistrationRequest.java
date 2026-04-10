package com.caffeine.acs_backend.dto.preregistration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Request to create a new pre-registration")
public record CreatePreRegistrationRequest(
    @NotNull UUID personId,
    @NotNull @Schema(description = "Expected arrival date and time") LocalDateTime expectedArrival,
    @Schema(description = "Host user ID, defaults to current user") UUID hostId,
    @Schema(description = "Internal notes") String notes,
    @NotNull @Schema(description = "Building (access point) ID") UUID buildingId) {}
