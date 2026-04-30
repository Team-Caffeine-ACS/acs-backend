package com.caffeine.acs_backend.dto.visitgroup;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Full details of a group visit including all members")
public record GroupVisitResponse(
    @Schema(description = "GroupInVisit ID") UUID groupInVisitId,
    @Schema(description = "Group name") String groupName,
    @Schema(description = "Group description") String groupDescription,
    @Schema(description = "Planned arrival time") LocalDateTime plannedArrival,
    @Schema(description = "Planned exit time") LocalDateTime plannedExit,
    @Schema(description = "Comment / purpose of the group visit") String comment,
    @Schema(description = "Building name") String building,
    @Schema(description = "Host full name") String hostName,
    @Schema(description = "Total number of members") int memberCount,
    @Schema(description = "Number of members currently checked in") int checkedInCount,
    @Schema(description = "Number of members who have departed") int departedCount,
    @Schema(description = "Individual group members with their statuses") List<GroupMemberResponse> members) {}
