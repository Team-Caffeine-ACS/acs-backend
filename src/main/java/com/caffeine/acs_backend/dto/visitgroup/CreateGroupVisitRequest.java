package com.caffeine.acs_backend.dto.visitgroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request to create a group visit with multiple visitors")
public record CreateGroupVisitRequest(
    @Schema(description = "Name of the visitor group", example = "Acme Corp delegation")
        @NotBlank
        @Size(max = 128)
        String groupName,
    @Schema(description = "Optional group description") @Size(max = 1024) String groupDescription,
    @Schema(description = "List of person IDs to include in the group visit") @NotEmpty
        List<UUID> personIds,
    @Schema(description = "Expected arrival time") @NotNull LocalDateTime expectedArrival,
    @Schema(description = "Expected exit time — optional") LocalDateTime expectedExit,
    @Schema(description = "Host person ID — optional") UUID hostId,
    @Schema(description = "Building (access point) ID") @NotNull UUID buildingId,
    @Schema(description = "Purpose or notes about the group visit") @Size(max = 1024)
        String comment) {}
