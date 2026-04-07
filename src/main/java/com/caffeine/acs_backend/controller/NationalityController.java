package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.nationality.NationalityResponse;
import com.caffeine.acs_backend.service.NationalityService;
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
@RequestMapping("/api/nationalities")
@RequiredArgsConstructor
@Tag(name = "Nationalities", description = "Reference data — nationality list for visitor form")
@SecurityRequirement(name = "bearerAuth")
public class NationalityController {

  private final NationalityService nationalityService;

  @Operation(
      summary = "List all nationalities",
      description = "Returns all allowed nationalities sorted alphabetically.")
  @ApiResponse(responseCode = "200", description = "Nationality list returned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @GetMapping
  public ResponseEntity<List<NationalityResponse>> getAllNationalities() {
    return ResponseEntity.ok(nationalityService.getAllNationalities());
  }
}
