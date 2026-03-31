package com.caffeine.acs_backend.dto.visit;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Result of a successfully recorded visit")
public record CreateVisitResponse(
    @Schema(description = "Created visit ID") UUID visitId,
    @Schema(description = "Person ID of the visitor") UUID personId,
    @Schema(description = "PersonInRole ID representing the visitor role assignment") UUID personInRoleId,
    @Schema(description = "KeycardInPossession record ID") UUID keycardInPossessionId,
    @Schema(description = "Keycard number that was assigned", example = "CARD-0042") String keycardNumber,
    @Schema(description = "Recorded arrival time") LocalDateTime arrivalTime) {}
