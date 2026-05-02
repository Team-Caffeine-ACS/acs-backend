package com.caffeine.acs_backend.dto.visitgroup;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Summary of a group visit for list views")
public record GroupVisitListItemResponse(
    @Schema(description = "GroupInVisit ID") UUID groupInVisitId,
    @Schema(description = "Group name") String groupName,
    @Schema(description = "Planned arrival time") LocalDateTime plannedArrival,
    @Schema(description = "Planned exit time") LocalDateTime plannedExit,
    @Schema(description = "Comment") String comment,
    @Schema(description = "Total number of members") int memberCount,
    @Schema(description = "Number of members currently checked in") int checkedInCount,
    @Schema(description = "Number of members who have departed") int departedCount) {}
