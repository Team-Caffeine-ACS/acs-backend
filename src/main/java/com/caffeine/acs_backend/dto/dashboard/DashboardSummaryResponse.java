package com.caffeine.acs_backend.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

public record DashboardSummaryResponse(
    @Schema(example = "12") long activeVisitors,
    @Schema(example = "25") long bookingsToday,
    @Schema(example = "3") long pendingRequests,
    @Schema(example = "150") long availableSpaces,
    @Schema(description = "Trend indicators compared to yesterday") Map<String, String> trendIndicators
) {}