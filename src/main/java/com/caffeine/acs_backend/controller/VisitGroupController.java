package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.visitgroup.CreateGroupVisitRequest;
import com.caffeine.acs_backend.dto.visitgroup.GroupVisitListItemResponse;
import com.caffeine.acs_backend.dto.visitgroup.GroupVisitResponse;
import com.caffeine.acs_backend.service.VisitGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visit-groups")
@RequiredArgsConstructor
@Tag(name = "Visit Groups", description = "Group visit management")
@SecurityRequirement(name = "bearerAuth")
public class VisitGroupController {

  private static final String ADMIN_OR_SECURITY_CHIEF = "hasAnyRole('ADMIN', 'SECURITY_CHIEF')";
  private static final String ADMIN_SECURITY_CHIEF_OR_RECEPTIONIST =
      "hasAnyRole('ADMIN', 'SECURITY_CHIEF', 'RECEPTIONIST')";

  private final VisitGroupService visitGroupService;

  @Operation(
      summary = "Create a group visit",
      description =
          "Creates a group visit with multiple visitors. Each visitor gets an individual"
              + " visit record with PRE_REGISTERED status linked to the same group.")
  @ApiResponse(responseCode = "201", description = "Group visit created")
  @ApiResponse(responseCode = "400", description = "Invalid request")
  @PreAuthorize(ADMIN_SECURITY_CHIEF_OR_RECEPTIONIST)
  @PostMapping
  public ResponseEntity<GroupVisitResponse> create(
      @Valid @RequestBody CreateGroupVisitRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(visitGroupService.create(request));
  }

  @Operation(
      summary = "Get group visit details",
      description = "Returns full group visit details including all members and their statuses.")
  @ApiResponse(responseCode = "200", description = "Group visit found")
  @ApiResponse(responseCode = "404", description = "Group visit not found")
  @PreAuthorize(ADMIN_SECURITY_CHIEF_OR_RECEPTIONIST)
  @GetMapping("/{groupInVisitId}")
  public ResponseEntity<GroupVisitResponse> getById(@PathVariable UUID groupInVisitId) {
    return ResponseEntity.ok(visitGroupService.getById(groupInVisitId));
  }

  @Operation(
      summary = "List group visits",
      description =
          "Returns a paginated list of group visits."
              + " Filter by date and search by group name or comment.")
  @ApiResponse(responseCode = "200", description = "Page of group visits")
  @PreAuthorize(ADMIN_SECURITY_CHIEF_OR_RECEPTIONIST)
  @GetMapping
  public ResponseEntity<Page<GroupVisitListItemResponse>> getAll(
      @RequestParam(required = false) LocalDate date,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(visitGroupService.getAll(date, search, pageable));
  }

  @Operation(
      summary = "Cancel a group visit",
      description =
          "Cancels all PRE_REGISTERED visits in the group."
              + " Already checked-in or departed members are not affected.")
  @ApiResponse(responseCode = "204", description = "Group visit cancelled")
  @ApiResponse(responseCode = "404", description = "Group visit not found")
  @PreAuthorize(ADMIN_OR_SECURITY_CHIEF)
  @DeleteMapping("/{groupInVisitId}")
  public ResponseEntity<Void> cancel(@PathVariable UUID groupInVisitId) {
    visitGroupService.cancel(groupInVisitId);
    return ResponseEntity.noContent().build();
  }
}
