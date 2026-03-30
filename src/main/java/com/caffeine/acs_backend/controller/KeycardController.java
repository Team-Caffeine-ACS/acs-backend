package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.keycard.KeycardResponse;
import com.caffeine.acs_backend.service.KeycardService;
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
@RequestMapping("/api/keycards")
@RequiredArgsConstructor
@Tag(name = "Keycards", description = "Keycard management")
@SecurityRequirement(name = "bearerAuth")
public class KeycardController {

  private final KeycardService keycardService;

  @Operation(
      summary = "List available keycards",
      description =
          "Returns all active keycards that are not currently assigned to anyone.")
  @ApiResponse(responseCode = "200", description = "Available keycard list returned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @GetMapping("/available")
  public ResponseEntity<List<KeycardResponse>> getAvailableKeycards() {
    return ResponseEntity.ok(keycardService.getAvailableKeycards());
  }
}
