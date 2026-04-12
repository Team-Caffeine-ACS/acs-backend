package com.caffeine.acs_backend.dto.visit;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Result of a successfully recorded visit")
public record CreateVisitResponse(
    @Schema(description = "Created visit ID") UUID visitId,
    @Schema(description = "Person ID of the visitor") UUID personId,
    @Schema(description = "PersonInRole ID representing the visitor role assignment")
        UUID personInRoleId,
    @Schema(description = "Visitor's first name") String firstName,
    @Schema(description = "Visitor's last name") String lastName,
    @Schema(description = "Personal ID code / social security number") String personalIdCode,
    @Schema(description = "Visitor's organization name, null if unaffiliated") String organization,
    @Schema(description = "Visitor's department name, null if unaffiliated") String department,
    @Schema(description = "Full name of the host, null if no host assigned") String hostName,
    @Schema(description = "Purpose or notes about the visit") String comment,
    @Schema(description = "Recorded arrival time") LocalDateTime arrivalTime,
    @Schema(description = "ID of the keycard currently held by the visitor, null if none")
        UUID cardId,
    @Schema(description = "KeycardInPossession record ID") UUID keycardInPossessionId,
    @Schema(description = "Keycard number that was assigned", example = "CARD-0042")
        String keycardNumber) {}
