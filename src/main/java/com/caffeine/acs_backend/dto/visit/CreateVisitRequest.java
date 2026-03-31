package com.caffeine.acs_backend.dto.visit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Request body for recording a visit and assigning a keycard")
public record CreateVisitRequest(
    @Schema(description = "ID of the person visiting") @NotNull UUID personId,
    @Schema(description = "Access point where the visit takes place") @NotNull UUID accessPointId,
    @Schema(description = "ID of the available keycard to assign") @NotNull UUID keycardId,
    @Schema(description = "PersonInRole ID of the host — optional") UUID hostPersonInRoleId,
    @Schema(description = "Purpose or notes about the visit", example = "Q1 review meeting")
        @Size(max = 1024)
        String comment,
    @Schema(description = "Arrival time — defaults to current time if not provided")
        LocalDateTime arrivalTime) {}
