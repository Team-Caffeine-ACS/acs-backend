package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.preregistration.CreatePreRegistrationRequest;
import com.caffeine.acs_backend.dto.preregistration.CreatePreRegistrationResponse;
import com.caffeine.acs_backend.dto.preregistration.NotifyRequest;
import com.caffeine.acs_backend.dto.preregistration.PreRegistrationResponse;
import com.caffeine.acs_backend.dto.preregistration.UpdatePreRegistrationRequest;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.service.PreRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pre-registrations")
@RequiredArgsConstructor
@Tag(name = "Pre-Registrations", description = "Visitor pre-registration management")
public class PreRegistrationController {

  private final PreRegistrationService preRegistrationService;

  @Operation(summary = "Create a new pre-registration")
  @ApiResponse(responseCode = "201", description = "Pre-registration created")
  @ApiResponse(responseCode = "400", description = "Invalid request")
  @PostMapping
  public ResponseEntity<CreatePreRegistrationResponse> create(
      @Valid @RequestBody CreatePreRegistrationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(preRegistrationService.create(request));
  }

  @Operation(summary = "Get pre-registration by ID")
  @ApiResponse(responseCode = "200", description = "Pre-registration found")
  @ApiResponse(responseCode = "404", description = "Not found")
  @GetMapping("/{id}")
  public ResponseEntity<PreRegistrationResponse> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(preRegistrationService.getById(id));
  }

  @Operation(summary = "Get paginated list of pre-registrations")
  @GetMapping
  public ResponseEntity<Page<PreRegistrationResponse>> getAll(
      @RequestParam(required = false) LocalDate date,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) VisitStatus status,
      @RequestParam(required = false) UUID buildingId,
      Pageable pageable) {
    return ResponseEntity.ok(preRegistrationService.getAll(date, search, status, buildingId, pageable));
  }

  @Operation(summary = "Update a pre-registration")
  @ApiResponse(responseCode = "200", description = "Updated successfully")
  @ApiResponse(responseCode = "404", description = "Not found")
  @PutMapping("/{id}")
  public ResponseEntity<PreRegistrationResponse> update(
      @PathVariable UUID id,
      @Valid @RequestBody UpdatePreRegistrationRequest request) {
    return ResponseEntity.ok(preRegistrationService.update(id, request));
  }

  @Operation(summary = "Cancel a pre-registration")
  @ApiResponse(responseCode = "204", description = "Cancelled successfully")
  @ApiResponse(responseCode = "404", description = "Not found")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> cancel(
      @PathVariable UUID id,
      @RequestParam(required = false) String message) {
    preRegistrationService.cancel(id, message);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Resend visitor notification email")
  @ApiResponse(responseCode = "204", description = "Email sent")
  @ApiResponse(responseCode = "400", description = "No email on file")
  @PostMapping("/{id}/notify")
  public ResponseEntity<Void> notify(
      @PathVariable UUID id,
      @RequestBody(required = false) NotifyRequest request) {
    preRegistrationService.resendNotification(id, request);
    return ResponseEntity.noContent().build();
  }
}