package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.keycard.AssignKeycardRequest;
import com.caffeine.acs_backend.dto.keycard.CreateKeycardRequest;
import com.caffeine.acs_backend.dto.keycard.KeycardDetailResponse;
import com.caffeine.acs_backend.dto.keycard.KeycardListItemResponse;
import com.caffeine.acs_backend.dto.keycard.ReturnKeycardRequest;
import com.caffeine.acs_backend.dto.keycard.UpdateKeycardRequest;
import com.caffeine.acs_backend.service.KeycardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/keycards")
@RequiredArgsConstructor
@Tag(name = "Keycards", description = "Keycard inventory and assignment management")
@SecurityRequirement(name = "bearerAuth")
public class KeycardController {

  private final KeycardService keycardService;

  @Operation(
      summary = "List keycards",
      description =
          "Returns a paginated list of keycards. Filter by status (available, in_use, disabled)."
              + " Search by card number, holder name, or department.")
  @ApiResponse(responseCode = "200", description = "Keycard list returned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @GetMapping
  public ResponseEntity<Page<KeycardListItemResponse>> getKeycards(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(keycardService.getKeycards(search, status, pageable));
  }

  @Operation(
      summary = "Get keycard details",
      description = "Returns full details for a single keycard.")
  @ApiResponse(responseCode = "200", description = "Keycard returned")
  @ApiResponse(responseCode = "404", description = "Keycard not found")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @GetMapping("/{cardId}")
  public ResponseEntity<KeycardDetailResponse> getKeycard(@PathVariable UUID cardId) {
    return ResponseEntity.ok(keycardService.getKeycard(cardId));
  }

  @Operation(
      summary = "Register new keycard",
      description = "Registers a new RFID keycard in the system.")
  @ApiResponse(responseCode = "201", description = "Keycard created")
  @ApiResponse(responseCode = "409", description = "Keycard number already exists")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @PostMapping
  public ResponseEntity<KeycardDetailResponse> createKeycard(
      @Valid @RequestBody CreateKeycardRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(keycardService.createKeycard(request));
  }

  @Operation(
      summary = "Update keycard",
      description = "Updates keycard metadata — number, active status, or expiry.")
  @ApiResponse(responseCode = "200", description = "Keycard updated")
  @ApiResponse(responseCode = "404", description = "Keycard not found")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @PutMapping("/{cardId}")
  public ResponseEntity<KeycardDetailResponse> updateKeycard(
      @PathVariable UUID cardId, @Valid @RequestBody UpdateKeycardRequest request) {
    return ResponseEntity.ok(keycardService.updateKeycard(cardId, request));
  }

  @Operation(
      summary = "Assign keycard",
      description = "Issues a keycard to a person. Records the holder, assignor, and access point.")
  @ApiResponse(responseCode = "200", description = "Keycard assigned")
  @ApiResponse(responseCode = "404", description = "Keycard, person, or access point not found")
  @ApiResponse(responseCode = "409", description = "Keycard already assigned or not active")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @PostMapping("/{cardId}/assign")
  public ResponseEntity<KeycardDetailResponse> assignKeycard(
      @PathVariable UUID cardId, @Valid @RequestBody AssignKeycardRequest request) {
    return ResponseEntity.ok(keycardService.assignKeycard(cardId, request));
  }

  @Operation(
      summary = "Return keycard",
      description =
          "Marks a keycard as returned. Records the return access point and current time.")
  @ApiResponse(responseCode = "200", description = "Keycard returned")
  @ApiResponse(responseCode = "404", description = "Keycard or access point not found")
  @ApiResponse(responseCode = "409", description = "Keycard is not currently assigned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @PostMapping("/{cardId}/return")
  public ResponseEntity<KeycardDetailResponse> returnKeycard(
      @PathVariable UUID cardId, @Valid @RequestBody ReturnKeycardRequest request) {
    return ResponseEntity.ok(keycardService.returnKeycard(cardId, request));
  }
}
