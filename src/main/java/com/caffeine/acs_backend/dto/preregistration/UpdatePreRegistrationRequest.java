package com.caffeine.acs_backend.dto.preregistration;

import com.caffeine.acs_backend.enums.VisitAccessLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Request to update a pre-registration")
public record UpdatePreRegistrationRequest(
    @Schema(description = "Updated expected arrival") LocalDateTime expectedArrival,
    @Schema(description = "Updated notes") String notes,
    @Schema(description = "Updated access level") VisitAccessLevel VisitAccessLevel) {}
