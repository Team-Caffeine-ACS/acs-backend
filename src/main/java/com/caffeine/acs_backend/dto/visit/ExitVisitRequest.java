package com.caffeine.acs_backend.dto.visit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Request body for marking a visitor as departed")
public record ExitVisitRequest(
    @Schema(description = "Exit time") @NotNull LocalDateTime exitTime) {}
