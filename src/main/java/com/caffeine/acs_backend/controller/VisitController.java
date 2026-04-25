package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.visit.CreateVisitRequest;
import com.caffeine.acs_backend.dto.visit.CreateVisitResponse;
import com.caffeine.acs_backend.dto.visit.EditVisitRequest;
import com.caffeine.acs_backend.dto.visit.ExitVisitRequest;
import com.caffeine.acs_backend.dto.visit.VisitDetailResponse;
import com.caffeine.acs_backend.dto.visit.VisitListItemResponse;
import com.caffeine.acs_backend.dto.visit.VisitTimelineEntry;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
@Tag(name = "Visits", description = "Visit management")
@SecurityRequirement(name = "bearerAuth")
public class VisitController {

  private static final String ADMIN_OR_SECURITY_CHIEF = "hasAnyRole('ADMIN', 'SECURITY_CHIEF')";
  private static final String ADMIN_SECURITY_CHIEF_OR_RECEPTIONIST =
      "hasAnyRole('ADMIN', 'SECURITY_CHIEF', 'RECEPTIONIST')";
  private final VisitService visitService;

  @Operation(
      summary = "List visits",
      description =
          "Returns a paginated list of visits."
              + " Filter by status (planned, in_building, departed, expired) and date range."
              + " Search by visitor name, document number, or host name."
              + " Accessible by Admin, Security Chief, and Receptionist.")
  @ApiResponse(responseCode = "200", description = "Visit list returned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @GetMapping
  @PreAuthorize(ADMIN_SECURITY_CHIEF_OR_RECEPTIONIST)
  public ResponseEntity<Page<VisitListItemResponse>> getVisits(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateTo,
      @RequestParam(required = false) UUID accessPoint,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "entryTime,desc") String sort) {

    // Kaitse liiga suurte väärtuste vastu (Swagger bug fix)
    if (size > 1000) {
      size = 1000;
    }
    if (page < 0) {
      page = 0;
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by(parseSort(sort)));

    return ResponseEntity.ok(
        visitService.getVisits(search, status, dateFrom, dateTo, accessPoint, pageable));
  }

  @Operation(
      summary = "Get visit details",
      description =
          "Returns full details for a single visit including visitor info, host, and assigned card."
              + " Accessible by Admin, Security Chief, and Receptionist.")
  @ApiResponse(responseCode = "200", description = "Visit returned")
  @ApiResponse(responseCode = "404", description = "Visit not found")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @GetMapping("/{visitId}")
  @PreAuthorize(ADMIN_SECURITY_CHIEF_OR_RECEPTIONIST)
  public ResponseEntity<VisitDetailResponse> getVisit(@PathVariable UUID visitId) {
    return ResponseEntity.ok(visitService.getVisit(visitId));
  }

  @Operation(
      summary = "Record visitor departure",
      description =
          "Sets the exit time on a visit, marking the visitor as departed."
              + " Accessible by Admin, Security Chief, and Receptionist.")
  @ApiResponse(responseCode = "200", description = "Exit recorded")
  @ApiResponse(responseCode = "404", description = "Visit not found")
  @ApiResponse(responseCode = "409", description = "Visit already has an exit time")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @PutMapping("/{visitId}/exit")
  @PreAuthorize(ADMIN_SECURITY_CHIEF_OR_RECEPTIONIST)
  public ResponseEntity<VisitDetailResponse> exitVisit(
      @PathVariable UUID visitId, @Valid @RequestBody ExitVisitRequest request) {
    return ResponseEntity.ok(visitService.exitVisit(visitId, request));
  }

  @Operation(
      summary = "Edit visit details",
      description =
          "Updates the host, assignor, access point, entry time, and comment on an existing visit."
              + " Sending hostId as null clears the host."
              + " Accessible by Admin and Security Chief only.")
  @ApiResponse(responseCode = "200", description = "Visit updated")
  @ApiResponse(
      responseCode = "404",
      description = "Visit, assignor, access point, or host not found")
  @ApiResponse(
      responseCode = "409",
      description = "Business rule violation, for example entry time after recorded exit time")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @PutMapping("/{visitId}/edit")
  @PreAuthorize(ADMIN_OR_SECURITY_CHIEF)
  public ResponseEntity<VisitDetailResponse> editVisit(
      @PathVariable UUID visitId, @Valid @RequestBody EditVisitRequest request) {
    return ResponseEntity.ok(visitService.editVisit(visitId, request));
  }

  @Operation(
      summary = "Get visit timeline",
      description =
          "Returns the ordered list of audit events for a visit (arrival, departure, etc.)."
              + " Accessible by Admin, Security Chief, and Receptionist.")
  @ApiResponse(responseCode = "200", description = "Timeline returned")
  @ApiResponse(responseCode = "404", description = "Visit not found")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @GetMapping("/{visitId}/timeline")
  @PreAuthorize(ADMIN_SECURITY_CHIEF_OR_RECEPTIONIST)
  public ResponseEntity<List<VisitTimelineEntry>> getTimeline(@PathVariable UUID visitId) {
    return ResponseEntity.ok(visitService.getTimeline(visitId));
  }

  @Operation(
      summary = "Record a new visit",
      description =
          "Records a visit and optionally assigns a keycard to the visitor."
              + " The authenticated user acts as the assignor.")
  @ApiResponse(responseCode = "201", description = "Visit recorded")
  @ApiResponse(responseCode = "400", description = "Validation error or keycard already assigned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @PostMapping
  @PreAuthorize(ADMIN_SECURITY_CHIEF_OR_RECEPTIONIST)
  public ResponseEntity<CreateVisitResponse> createVisit(
      @Valid @RequestBody CreateVisitRequest request, @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(visitService.createVisit(request, currentUser));
  }

  private List<Sort.Order> parseSort(String sort) {
    String[] parts = sort.split(",");
    String property = parts[0].trim();

    Sort.Direction direction =
        (parts.length > 1 && parts[1].equalsIgnoreCase("asc"))
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

    return List.of(new Sort.Order(direction, property));
  }
}
