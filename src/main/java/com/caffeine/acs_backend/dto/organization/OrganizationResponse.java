package com.caffeine.acs_backend.dto.organization;

import com.caffeine.acs_backend.entity.Organization;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Organization record")
public record OrganizationResponse(
    @Schema(description = "Unique identifier") UUID id,
    @Schema(description = "Organization name", example = "Acme Corp") String name) {

  public static OrganizationResponse from(Organization o) {
    return new OrganizationResponse(o.getId(), o.getName());
  }
}
