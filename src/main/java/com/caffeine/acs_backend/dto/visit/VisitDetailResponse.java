package com.caffeine.acs_backend.dto.visit;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Full details for a single visit")
public record VisitDetailResponse(
    @Schema(description = "Visit ID") UUID visitId,
    @Schema(description = "Visitor's first name", example = "John") String firstName,
    @Schema(description = "Visitor's last name", example = "Smith") String lastName,
    @Schema(description = "Personal ID code / social security number") String personalIdCode,
    @Schema(description = "Visitor's organization name, null if unaffiliated") String organization,
    @Schema(description = "Visitor's department name, null if unaffiliated") String department,
    @Schema(description = "Full name of the host, null if no host assigned") String hostName,
    @Schema(description = "Purpose or notes about the visit") String visitReason,
    @Schema(description = "ID of the keycard currently held by the visitor, null if none")
        UUID cardId) {}
