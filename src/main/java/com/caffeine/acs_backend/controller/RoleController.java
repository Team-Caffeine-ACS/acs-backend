package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.dto.role.RoleResponse;
import com.caffeine.acs_backend.service.RoleService;
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
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

  private final RoleService roleService;

  @Operation(summary = "List all roles")
  @ApiResponse(responseCode = "200", description = "Role list returned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @GetMapping
  public ResponseEntity<List<RoleResponse>> getAll() {
    return ResponseEntity.ok(roleService.getAll());
  }

  @Operation(summary = "Create a role")
  @ApiResponse(responseCode = "201", description = "Role created")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @PostMapping
  public ResponseEntity<RoleResponse> create(@Valid @RequestBody LookupRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(request));
  }

  @Operation(summary = "Update a role")
  @ApiResponse(responseCode = "200", description = "Role updated")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @ApiResponse(responseCode = "404", description = "Role not found")
  @PutMapping("/{id}")
  public ResponseEntity<RoleResponse> update(
      @PathVariable UUID id, @Valid @RequestBody LookupRequest request) {
    return ResponseEntity.ok(roleService.update(id, request));
  }

  @Operation(summary = "Delete a role")
  @ApiResponse(responseCode = "204", description = "Role deleted")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @ApiResponse(responseCode = "404", description = "Role not found")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    roleService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
