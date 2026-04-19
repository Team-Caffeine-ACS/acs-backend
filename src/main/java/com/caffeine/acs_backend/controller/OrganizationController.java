package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.dto.organization.OrganizationResponse;
import com.caffeine.acs_backend.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class OrganizationController {

  private final OrganizationService organizationService;

  @Operation(summary = "List all organizations")
  @ApiResponse(responseCode = "200", description = "Organization list returned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @GetMapping
  public ResponseEntity<List<OrganizationResponse>> getAll() {
    return ResponseEntity.ok(organizationService.getAll());
  }

  @Operation(summary = "Create an organization")
  @ApiResponse(responseCode = "201", description = "Organization created")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @PostMapping
  public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody LookupRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(organizationService.create(request));
  }

  @Operation(summary = "Update an organization")
  @ApiResponse(responseCode = "200", description = "Organization updated")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @ApiResponse(responseCode = "404", description = "Organization not found")
  @PutMapping("/{id}")
  public ResponseEntity<OrganizationResponse> update(
      @PathVariable UUID id, @Valid @RequestBody LookupRequest request) {
    return ResponseEntity.ok(organizationService.update(id, request));
  }

  @Operation(summary = "Delete an organization")
  @ApiResponse(responseCode = "204", description = "Organization deleted")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @ApiResponse(responseCode = "404", description = "Organization not found")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    organizationService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
