package com.caffeine.acs_backend.dto.country;

import com.caffeine.acs_backend.entity.Country;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Country option for dropdown selection")
public record CountryResponse(
    @Schema(description = "Unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
    @Schema(description = "Country name", example = "Estonia") String name) {

  public static CountryResponse from(Country country) {
    return new CountryResponse(country.getId(), country.getName());
  }
}
