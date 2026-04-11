package com.caffeine.acs_backend.dto.visit;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "A single audit entry in the visit timeline")
public record VisitTimelineEntry(
    @Schema(description = "When the event occurred") LocalDateTime timestamp,
    @Schema(description = "Type of event", example = "ARRIVAL_REGISTERED") String eventType,
    @Schema(description = "Additional details or comments") String details) {}
