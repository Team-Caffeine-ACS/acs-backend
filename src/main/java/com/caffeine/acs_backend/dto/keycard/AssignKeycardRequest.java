package com.caffeine.acs_backend.dto.keycard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Request body for issuing a keycard to a person")
public record AssignKeycardRequest(
    @Schema(description = "PersonInRole ID of the person receiving the card") @NotNull
        UUID keycardHolderPersonInRoleId,
    @Schema(description = "PersonInRole ID of the staff member issuing the card") @NotNull
        UUID keycardAssignorPersonInRoleId,
    @Schema(description = "Access point ID where the card is being issued") @NotNull
        UUID assigningAccessPointId) {}
