package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.department.DepartmentResponse;
import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.service.DepartmentService;
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
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Departments")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class DepartmentController {

  private final DepartmentService departmentService;

  @Operation(summary = "List all departments")
  @ApiResponse(responseCode = "200", description = "Department list returned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @GetMapping
  public ResponseEntity<List<DepartmentResponse>> getAll() {
    return ResponseEntity.ok(departmentService.getAll());
  }

  @Operation(summary = "Create a department")
  @ApiResponse(responseCode = "201", description = "Department created")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @PostMapping
  public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody LookupRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(request));
  }

  @Operation(summary = "Update a department")
  @ApiResponse(responseCode = "200", description = "Department updated")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @ApiResponse(responseCode = "404", description = "Department not found")
  @PutMapping("/{id}")
  public ResponseEntity<DepartmentResponse> update(
      @PathVariable UUID id, @Valid @RequestBody LookupRequest request) {
    return ResponseEntity.ok(departmentService.update(id, request));
  }

  @Operation(summary = "Delete a department")
  @ApiResponse(responseCode = "204", description = "Department deleted")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @ApiResponse(responseCode = "404", description = "Department not found")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    departmentService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
