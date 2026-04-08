package com.caffeine.acs_backend.dto.preregistration;

import com.caffeine.acs_backend.enums.VisitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Response after creating a pre-registration")
public record CreatePreRegistrationResponse(
    @Schema(description = "ID of the created pre-registration") UUID preRegistrationId,
    @Schema(description = "Confirmation status") VisitStatus confirmationStatus) {}