package com.caffeine.acs_backend.dto.accesspoint;

import com.caffeine.acs_backend.entity.AccessPoint;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Access point option for dropdown selection")
public record AccessPointResponse(
    @Schema(description = "Unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
    @Schema(description = "Name of the access point", example = "Main entrance") String name,
    @Schema(description = "Physical address", example = "Building A, Gate 1") String address,
    @Schema(description = "Latitude for Google Maps", example = "59.437000") BigDecimal latitude,
    @Schema(description = "Longitude for Google Maps", example = "24.753600")
        BigDecimal longitude) {

  public static AccessPointResponse from(AccessPoint accessPoint) {
    return new AccessPointResponse(
        accessPoint.getId(),
        accessPoint.getName(),
        accessPoint.getAddress(),
        accessPoint.getLatitude(),
        accessPoint.getLongitude());
  }
}
