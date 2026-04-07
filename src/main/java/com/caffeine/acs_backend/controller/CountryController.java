package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.country.CountryResponse;
import com.caffeine.acs_backend.service.CountryService;
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
@RequestMapping("/api/countries")
@RequiredArgsConstructor
@Tag(name = "Countries", description = "Reference data — country list for document forms")
@SecurityRequirement(name = "bearerAuth")
public class CountryController {

  private final CountryService countryService;

  @Operation(
      summary = "List all countries",
      description = "Returns all countries sorted alphabetically.")
  @ApiResponse(responseCode = "200", description = "Country list returned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @GetMapping
  public ResponseEntity<List<CountryResponse>> getAllCountries() {
    return ResponseEntity.ok(countryService.getAllCountries());
  }
}
