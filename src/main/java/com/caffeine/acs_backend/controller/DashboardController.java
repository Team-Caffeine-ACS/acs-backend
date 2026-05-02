package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.dashboard.DashboardSummaryResponse;
import com.caffeine.acs_backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints for dashboard metrics and activity")
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping("/summary")
  @Operation(summary = "Get today's summary metrics")
  public ResponseEntity<DashboardSummaryResponse> getSummary(
      @RequestParam(required = false) UUID accessPointId) {
    return ResponseEntity.ok(dashboardService.getSummary(accessPointId));
  }
}
