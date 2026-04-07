package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.accesspoint.AccessPointResponse;
import com.caffeine.acs_backend.service.AccessPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/access-points")
@RequiredArgsConstructor
@Tag(name = "Access Points")
@SecurityRequirement(name = "bearerAuth")
public class AccessPointController {

  private final AccessPointService accessPointService;

  @Operation(
      summary = "List all access points",
      description = "Returns all access points sorted alphabetically")
  @ApiResponse(responseCode = "200", description = "Access point list returned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @GetMapping
  public ResponseEntity<List<AccessPointResponse>> getAllAccessPoints() {
    return ResponseEntity.ok(accessPointService.getAllAccessPoints());
  }
}
