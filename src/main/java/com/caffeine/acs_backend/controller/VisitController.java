package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.visit.CreateVisitRequest;
import com.caffeine.acs_backend.dto.visit.CreateVisitResponse;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
@Tag(name = "Visits", description = "Visit registration")
@SecurityRequirement(name = "bearerAuth")
public class VisitController {

  private final VisitService visitService;

  @Operation(
      summary = "Record a new visit",
      description =
          "Records a visit and assigns a keycard to an visitor"
              + " The authenticated user acts as the assignor.")
  @ApiResponse(responseCode = "201", description = "Visit recorded and keycard assigned for person")
  @ApiResponse(responseCode = "400", description = "Validation error or keycard already assigned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @PostMapping
  public ResponseEntity<CreateVisitResponse> createVisit(
      @Valid @RequestBody CreateVisitRequest request, @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(visitService.createVisit(request, currentUser));
  }
}
