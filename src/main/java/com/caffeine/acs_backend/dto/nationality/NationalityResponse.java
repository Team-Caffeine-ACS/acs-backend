package com.caffeine.acs_backend.dto.nationality;

import com.caffeine.acs_backend.entity.Nationality;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Nationality option for dropdown selection")
public record NationalityResponse(
    @Schema(description = "Unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
    @Schema(description = "Country name", example = "Estonia") String name,
    @Schema(description = "ISO 3166-1 alpha-2 country code", example = "EE") String countryCode) {

  public static NationalityResponse from(Nationality nationality) {
    return new NationalityResponse(
        nationality.getId(), nationality.getName(), nationality.getCountryCode());
  }
}
