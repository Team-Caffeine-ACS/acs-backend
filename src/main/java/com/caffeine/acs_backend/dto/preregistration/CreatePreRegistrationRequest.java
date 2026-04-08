package com.caffeine.acs_backend.dto.preregistration;

import com.caffeine.acs_backend.enums.VisitAccessLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Request to create a new pre-registration")
public record CreatePreRegistrationRequest(
    @NotBlank @Schema(description = "Full name of the visitor") String fullName,
    @Email @Schema(description = "Email of the visitor") String email,
    @NotNull @Schema(description = "Expected arrival date and time") LocalDateTime expectedArrival,
    @Schema(description = "Host user ID, defaults to current user") UUID hostId,
    @Schema(description = "Internal notes") String notes,
    @NotNull @Schema(description = "Access level") VisitAccessLevel VisitAccessLevel,
    @NotNull @Schema(description = "Building (access point) ID") UUID buildingId) {}
