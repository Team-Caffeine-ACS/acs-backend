package com.caffeine.acs_backend.dto.person;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Request body for creating a new person")
public record CreatePersonRequest(
    @Schema(description = "First name", example = "John") @NotBlank @Size(max = 128)
        String givenName,
    @Schema(description = "Last name", example = "Smith") @NotBlank @Size(max = 128) String surname,
    @Schema(description = "Nationality ID") @NotNull UUID nationalityId,
    @Schema(description = "Identity document type ID — required when documentNumber is provided")
        UUID documentTypeId,
    @Schema(description = "Document number (e.g. passport number)", example = "AB1234567")
        @Size(max = 128)
        String documentNumber) {}
