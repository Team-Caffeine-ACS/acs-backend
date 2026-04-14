package com.caffeine.acs_backend.dto.visit;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Recent visit information for dashboard display")
public record DasboardRecentVisitResponse (
    @Schema(description = "Full name of the visitor", example = "John Doe") String fullName,
    @Schema(description = "Organization of the visitor", example = "Acme Corp") String organization,
    @Schema(description = "Entry time of the visit", example = "2024-06-01T08:30:00") LocalDateTime entryTime,
    @Schema(description = "Exit time of the visit", example = "2024-06-01T10:30:00") LocalDateTime exitTime,
    @Schema(description = "Status of the visit", example = "Sees") String status, // "Sees", "Väljas", "Ootel"
    @Schema(description = "Unique identifier of the visitor", example = "550e8400-e29b-41d4-a716-446655440000") UUID visitorId
){
    
}
