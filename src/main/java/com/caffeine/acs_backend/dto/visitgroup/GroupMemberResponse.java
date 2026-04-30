package com.caffeine.acs_backend.dto.visitgroup;

import com.caffeine.acs_backend.enums.VisitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Details of a single member within a group visit")
public record GroupMemberResponse(
    @Schema(description = "Individual visit ID") UUID visitId,
    @Schema(description = "Person ID") UUID personId,
    @Schema(description = "Full name of the visitor") String fullName,
    @Schema(description = "Email of the visitor") String email,
    @Schema(description = "Personal ID code / document number") String personalIdCode,
    @Schema(description = "Current visit status") VisitStatus status,
    @Schema(description = "Actual arrival time, null if not yet checked in") LocalDateTime arrivalTime,
    @Schema(description = "Actual exit time, null if not yet departed") LocalDateTime exitTime) {}
