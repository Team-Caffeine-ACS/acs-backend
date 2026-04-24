package com.caffeine.acs_backend.dto.visit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Request body for editing visit details")
public record EditVisitRequest(
    @Schema(description = "Host person ID (person.id) — null to clear the host") UUID hostId,
    @Schema(description = "PersonInRole ID of the staff member making the edit") @NotNull
        UUID assignorId,
    @Schema(description = "Access point where the edit is being made") UUID accessPointId,
    @Schema(description = "Updated entry time") LocalDateTime entryTime,
    @Schema(description = "Updated exit/departure time") LocalDateTime exitTime,
    @Schema(description = "Updated visit purpose or notes") @Size(max = 1024) String comment) {}
