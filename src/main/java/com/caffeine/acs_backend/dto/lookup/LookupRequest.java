package com.caffeine.acs_backend.dto.lookup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for creating or updating a lookup record")
public record LookupRequest(
    @Schema(description = "Name of the record", example = "Engineering")
        @NotBlank(message = "Name must not be blank")
        String name) {}
